package org.rudi.microservice.kalim.service.integration.impl.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.apigateway.exceptions.DeleteApiException;
import org.rudi.facet.dataverse.api.exceptions.DatasetNotFoundException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.helper.dataset.metadatadetails.MetadataDetailsHelper;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.facet.providers.bean.LinkedProducer;
import org.rudi.facet.providers.bean.NodeProvider;
import org.rudi.facet.providers.bean.Organization;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.core.bean.IntegrationStatus;
import org.rudi.microservice.kalim.core.bean.Method;
import org.rudi.microservice.kalim.core.bean.ProgressStatus;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.service.KalimSpringBootTest;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.EmailHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@KalimSpringBootTest
class DeleteIntegrationRequestTreatmentHandlerUT {

	private final UserDataFactory userDataFactory;
	private final Error500Builder error500Builder = new Error500Builder();
	private final JsonResourceReader jsonResourceReader = new JsonResourceReader();
	private AbstractIntegrationRequestTreatmentHandler handler;

	@Mock
	private DatasetService datasetService;
	@Mock
	private ApiManagerHelper apigatewayManagerHelper;
	@Mock
	private MetadataDetailsHelper metadataDetailsHelper;
	@Mock
	ObjectMapper objectMapper;

	@MockitoBean
	ProviderHelper providerHelper;
	@MockitoBean
	ACLHelper aclHelper;
	@MockitoBean
	RolesHelper roleHelper;
	@MockitoBean
	ProjektHelper projektHelper;
	@MockitoBean
	EmailHelper emailHelper;

	@Autowired
	UtilPageable utilPageable;

	@BeforeEach
	void setUp() {
		handler = new DeleteIntegrationRequestTreatmentHandler(datasetService, apigatewayManagerHelper, error500Builder,
				metadataDetailsHelper, aclHelper, providerHelper, objectMapper, roleHelper, projektHelper, utilPageable, emailHelper);
	}

	private UUID init() {
		UUID nodeProviderUuid = UUID.randomUUID();
		NodeProvider nodeProvider = new NodeProvider().uuid(nodeProviderUuid);
		Provider provider = new Provider();
		provider.setNodeProviders(List.of(nodeProvider));
		Organization organization = new Organization();
		organization.setUuid(UUID.fromString("e6262c50-1628-436b-92f9-82b560729830"));
		organization.setName("TA RUDI-1460 a accepter");
		LinkedProducer linkedProducer = new LinkedProducer();
		linkedProducer.setUuid(UUID.randomUUID());
		linkedProducer.setOrganization(organization);
		provider.setLinkedProducers(List.of(linkedProducer));
		User user = userDataFactory.createUserNodeProvider(nodeProviderUuid.toString());

		when(aclHelper.getUserByUUID(any())).thenReturn(user);
		when(aclHelper.getUserByLogin(any())).thenReturn(user);
		when(providerHelper.getNodeProviderByUUID(any())).thenReturn(nodeProvider);
		when(providerHelper.getFullProviderByNodeProviderUUID(any())).thenReturn(provider);

		return nodeProviderUuid;
	}

	@Test
	@DisplayName("non existing metadata ⇒ error")
	void createIntegrationRequestDeleteNonExistingMetadata()
			throws DataverseAPIException, IOException, DeleteApiException {

		UUID nodeProviderUuid = init();

		final Metadata metadataToDelete = buildMetadataToDelete();
		final String metadataJson = jsonResourceReader.getObjectMapper().writeValueAsString(metadataToDelete);
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().method(Method.DELETE)
				.uuid(UUID.randomUUID()).globalId(metadataToDelete.getGlobalId()).progressStatus(ProgressStatus.CREATED)
				.errors(new HashSet<>()).file(metadataJson).nodeProviderId(nodeProviderUuid).build();

		when(datasetService.getDataset(metadataToDelete.getGlobalId()))
				.thenThrow(DatasetNotFoundException.fromGlobalId(metadataToDelete.getGlobalId()));

		handler.handle(integrationRequest);

		assertThat(integrationRequest)
				.as("La requête doit échouer")
				.matches(ir -> ir.getIntegrationStatus().equals(IntegrationStatus.KO))
				.as("Il doit y avoir une erreur dans le rapport")
				.matches(ir -> !ir.getErrors().isEmpty())
				.as("Au moins une de ces erreur doit être une erreur 108")
				.matches(ir -> ir.getErrors().stream()
						.anyMatch(
								e -> e.getCode().equals(IntegrationError.ERR_109.getCode())
						)
				);
	}

	private Metadata buildMetadataToDelete() throws IOException {
		return jsonResourceReader.read("metadata/creation-ok.json", Metadata.class);
	}

	/**
	 * RUDI-541 : On doit pouvoir supprimer un JDD sans devoir envoyer tout son contenu JSON
	 *
	 * @throws DeleteApiException
	 */
	@Test
	@DisplayName("existing metadata ⇒ dataset archived and API deleted")
	void createIntegrationRequestDeleteExistingMetadata()
			throws DataverseAPIException, IOException, DeleteApiException {

		UUID nodeProviderUuid = init();

		final Metadata metadataToDelete = buildMetadataToDelete();
		final String metadataJson = jsonResourceReader.getObjectMapper().writeValueAsString(metadataToDelete);
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().method(Method.DELETE)
				.uuid(UUID.randomUUID()).globalId(metadataToDelete.getGlobalId()).progressStatus(ProgressStatus.CREATED)
				.file(metadataJson).errors(new HashSet<>()).nodeProviderId(nodeProviderUuid).build();

		when(datasetService.getDataset(metadataToDelete.getGlobalId())).thenReturn(metadataToDelete);
		when(projektHelper.searchProjects(any(), any())).thenReturn(Page.empty());

		handler.handle(integrationRequest);

		assertThat(integrationRequest.getIntegrationStatus()).isEqualTo(IntegrationStatus.OK);

		// DataSet is archived
		verify(datasetService).archiveDataset(metadataToDelete.getDataverseDoi());

		// API is deleted
		verify(apigatewayManagerHelper).deleteApis(integrationRequest);
	}

	@Test
	@DisplayName("existing metadata ⇒ dataset archived and API deleted as Admin")
	void createIntegrationRequestDeleteExistingMetadataAsAdmin()
			throws DataverseAPIException, IOException, DeleteApiException {

		UUID nodeProviderUuid = init();

		final Metadata metadataToDelete = buildMetadataToDelete();
		final String metadataJson = jsonResourceReader.getObjectMapper().writeValueAsString(metadataToDelete);
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().method(Method.DELETE)
				.uuid(UUID.randomUUID()).globalId(metadataToDelete.getGlobalId()).progressStatus(ProgressStatus.CREATED)
				.file(metadataJson).errors(new HashSet<>()).nodeProviderId(nodeProviderUuid).build();

		when(datasetService.getDataset(metadataToDelete.getGlobalId())).thenReturn(metadataToDelete);
		when(projektHelper.searchProjects(any(), any())).thenReturn(Page.empty());

		handler.handle(integrationRequest);

		assertThat(integrationRequest.getIntegrationStatus()).isEqualTo(IntegrationStatus.OK);

		// DataSet is archived
		verify(datasetService).archiveDataset(metadataToDelete.getDataverseDoi());

		// API is deleted
		verify(apigatewayManagerHelper).deleteApis(integrationRequest);
	}



}
