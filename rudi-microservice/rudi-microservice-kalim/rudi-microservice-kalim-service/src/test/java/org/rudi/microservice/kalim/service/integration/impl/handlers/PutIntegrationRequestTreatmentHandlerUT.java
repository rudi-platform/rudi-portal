package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.core.json.DefaultJackson2ObjectMapperBuilder;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.apigateway.exceptions.ApiGatewayApiException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.facet.providers.bean.LinkedProducer;
import org.rudi.facet.providers.bean.NodeProvider;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.core.bean.IntegrationStatus;
import org.rudi.microservice.kalim.core.bean.Method;
import org.rudi.microservice.kalim.core.bean.ProgressStatus;
import org.rudi.microservice.kalim.service.KalimSpringBootTest;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.service.integration.impl.transformer.metadata.AbstractMetadataTransformer;
import org.rudi.microservice.kalim.service.integration.impl.validator.authenticated.MetadataInfoProviderIsAuthenticatedValidator;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.AbstractMetadataValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@KalimSpringBootTest
class PutIntegrationRequestTreatmentHandlerUT {

	private final UserDataFactory userDataFactory;
	private final ObjectMapper objectMapper = new DefaultJackson2ObjectMapperBuilder().build();
	private final Error500Builder error500Builder = new Error500Builder();
	private final JsonResourceReader jsonResourceReader = new JsonResourceReader();
	private AbstractIntegrationRequestTreatmentHandler handler;
	@Mock
	private AbstractMetadataValidator<?> validator;
	@Mock
	private AbstractMetadataTransformer<?> metadataTransformer;
	@Mock
	private DatasetService datasetService;
	@Mock
	private ApiManagerHelper apiGatewayManagerHelper;
	@Mock
	private MetadataInfoProviderIsAuthenticatedValidator metadataInfoProviderIsAuthenticatedValidator;
	@Mock
	private OrganizationHelper organizationHelper;
	@Captor
	private ArgumentCaptor<Metadata> metadataArgumentCaptor;
	@MockitoBean
	ProviderHelper providerHelper;
	@MockitoBean
	ACLHelper aclHelper;
	@MockitoBean
	RolesHelper roleHelper;

	@BeforeEach
	void setUp() {
		handler = new PutIntegrationRequestTreatmentHandler(datasetService, apiGatewayManagerHelper, objectMapper,
				Collections.singletonList(validator), Collections.singletonList(metadataTransformer), error500Builder,
				metadataInfoProviderIsAuthenticatedValidator, organizationHelper, providerHelper, aclHelper,
				roleHelper);

		when(validator.canBeUsedBy(handler)).thenReturn(true);
	}

	private UUID init(){
		UUID nodeProviderUuid = UUID.randomUUID();
		NodeProvider nodeProvider = new NodeProvider().uuid(nodeProviderUuid);

		Provider provider = new Provider();
		provider.setUuid(UUID.randomUUID());
		provider.setNodeProviders(List.of(nodeProvider));

		org.rudi.facet.providers.bean.Organization o1 = new org.rudi.facet.providers.bean.Organization();
		o1.setUuid(UUID.fromString("e6262c50-1628-436b-92f9-82b560729830"));
		o1.setName("TA RUDI-1460 a accepter");
		LinkedProducer lp1 = new LinkedProducer();
		lp1.setUuid(UUID.randomUUID());
		lp1.setOrganization(o1);

		org.rudi.facet.providers.bean.Organization o2 = new org.rudi.facet.providers.bean.Organization();
		o2.setUuid(UUID.fromString("acdccf43-566b-4134-b39e-ddf46c801242"));
		o2.setName("Producteur rudi");
		LinkedProducer lp2 = new LinkedProducer();
		lp2.setUuid(UUID.randomUUID());
		lp2.setOrganization(o2);

		provider.setLinkedProducers(List.of(lp1, lp2));
		User user = userDataFactory.createUserNodeProvider(nodeProviderUuid.toString());

		when(aclHelper.getUserByLogin(any())).thenReturn(user);
		when(providerHelper.getNodeProviderByUUID(any())).thenReturn(nodeProvider);
		when(providerHelper.getFullProviderByNodeProviderUUID(any())).thenReturn(provider);

		return nodeProviderUuid;
	}

	@Test
	@DisplayName("validation failed ❌ ⇒ stop \uD83D\uDED1")
	void handleValidationErrorNoUpdate() throws IOException {
		UUID nodeProvicerUuid = init();
		final Metadata metadataToUpdate = buildMetadataToUpdate();
		final String metadataJson = jsonResourceReader.getObjectMapper().writeValueAsString(metadataToUpdate);
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().method(Method.PUT)
				.uuid(UUID.randomUUID()).globalId(metadataToUpdate.getGlobalId()).progressStatus(ProgressStatus.CREATED)
				.file(metadataJson).errors(new HashSet<>()).nodeProviderId(nodeProvicerUuid).build();

		final Set<IntegrationRequestErrorEntity> errors = Collections.singleton(new IntegrationRequestErrorEntity());
		when(validator.validateMetadata(any(Metadata.class))).thenReturn(errors);

		handler.handle(integrationRequest);

		assertThat(integrationRequest.getIntegrationStatus()).isEqualTo(IntegrationStatus.KO);

		// If validation fails, integration request should not go any further
		verifyNoMoreInteractions(datasetService);
		verifyNoInteractions(apiGatewayManagerHelper);
	}

	@Test
	@DisplayName("validation passed ✔ ⇒ dataset and API updated \uD83E\uDD73")
	void handleNoValidationErrorUpdate() throws DataverseAPIException, ApiGatewayApiException, IOException, GetOrganizationException {
		UUID nodeProvicerUuid = init();
		final Metadata metadataToUpdate = buildMetadataToUpdate();
		final String metadataJson = jsonResourceReader.getObjectMapper().writeValueAsString(metadataToUpdate);
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().method(Method.PUT)
				.uuid(UUID.randomUUID()).globalId(metadataToUpdate.getGlobalId()).progressStatus(ProgressStatus.CREATED)
				.file(metadataJson).errors(new HashSet<>()).nodeProviderId(nodeProvicerUuid).build();

		final Set<IntegrationRequestErrorEntity> errors = Collections.emptySet();
		when(validator.validateMetadata(metadataArgumentCaptor.capture())).thenReturn(errors);

		final Metadata updatedMetadata = mock(Metadata.class);
		when(datasetService.getDataset(metadataToUpdate.getGlobalId())).thenReturn(metadataToUpdate);
		when(datasetService.updateDataset(metadataToUpdate)).thenReturn(updatedMetadata);

		handler.handle(integrationRequest);

		assertThat(integrationRequest.getIntegrationStatus()).isEqualTo(IntegrationStatus.OK);

		// If validation succeeds, API should be updated
		verify(apiGatewayManagerHelper).updateApis(integrationRequest, updatedMetadata, metadataToUpdate);
	}

	@Test
	@DisplayName("non existing metadata ⇒ error \uD83D\uDED1")
	void handleNonExistingMetadata() throws IOException {
		UUID nodeProvicerUuid = init();
		final Metadata metadataToUpdate = buildMetadataToUpdate();
		final String metadataJson = jsonResourceReader.getObjectMapper().writeValueAsString(metadataToUpdate);
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().method(Method.PUT)
				.uuid(UUID.randomUUID()).globalId(metadataToUpdate.getGlobalId()).progressStatus(ProgressStatus.CREATED)
				.file(metadataJson).errors(new HashSet<>()).nodeProviderId(nodeProvicerUuid).build();

		final IntegrationRequestErrorEntity nonExistingError = mock(IntegrationRequestErrorEntity.class);
		final Set<IntegrationRequestErrorEntity> errors = Collections.singleton(nonExistingError);
		when(validator.validateMetadata(metadataArgumentCaptor.capture())).thenReturn(errors);

		handler.handle(integrationRequest);

		assertThat(integrationRequest.getIntegrationStatus()).isEqualTo(IntegrationStatus.KO);

		// If validation fails, there is no interaction
		verifyNoInteractions(datasetService);
		verifyNoInteractions(apiGatewayManagerHelper);
	}

	@Test
	@DisplayName("API Gateway error ❌ ⇒ dataset updated rollback")
	void handleValidationApigatewayErrorNoUpdate() throws DataverseAPIException, ApiGatewayApiException, IOException {
		UUID nodeProvicerUuid = init();
		final Metadata actualMetadata = buildMetadataBeforeUpdate();
		final Metadata metadataToUpdate = buildMetadataToUpdate();
		final String metadataToUpdateJson = jsonResourceReader.getObjectMapper().writeValueAsString(metadataToUpdate);
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().method(Method.PUT)
				.uuid(UUID.randomUUID()).globalId(metadataToUpdate.getGlobalId()).progressStatus(ProgressStatus.CREATED)
				.file(metadataToUpdateJson).errors(new HashSet<>()).nodeProviderId(nodeProvicerUuid).build();

		final Set<IntegrationRequestErrorEntity> errors = Collections.emptySet();
		when(validator.validateMetadata(any(Metadata.class))).thenReturn(errors);

		when(datasetService.getDataset(metadataToUpdate.getGlobalId())).thenReturn(actualMetadata);
		when(datasetService.updateDataset(metadataToUpdate)).thenReturn(metadataToUpdate);
		when(datasetService.updateDataset(actualMetadata)).thenReturn(actualMetadata);

		doThrow(new ApiGatewayApiException(WebClientResponseException.create(404, "not found", null, null, null)))
				.when(apiGatewayManagerHelper).updateApis(eq(integrationRequest), any(), any());

		handler.handle(integrationRequest);

		assertThat(integrationRequest.getIntegrationStatus())
				.as("L'intégration est KO car l'API Gateway a renvoyé une erreur").isEqualTo(IntegrationStatus.KO);

		InOrder inOrderToVerifyDatasetServiceUpdateCall = inOrder(datasetService);
		// Appel de la mise à jour des métadonnées
		inOrderToVerifyDatasetServiceUpdateCall.verify(datasetService).updateDataset(metadataToUpdate);
		// Appel de la mise à jour pour restaurer les métadonnées
		inOrderToVerifyDatasetServiceUpdateCall.verify(datasetService).updateDataset(actualMetadata);
	}

	private Metadata buildMetadataBeforeUpdate() throws IOException {
		return jsonResourceReader.read("metadata/creation-ok.json", Metadata.class);
	}

	private Metadata buildMetadataToUpdate() throws IOException {
		return jsonResourceReader.read("metadata/modification-ok.json", Metadata.class);
	}

}
