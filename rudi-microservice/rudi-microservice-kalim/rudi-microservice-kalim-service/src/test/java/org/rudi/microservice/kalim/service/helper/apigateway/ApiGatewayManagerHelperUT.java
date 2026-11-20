package org.rudi.microservice.kalim.service.helper.apigateway;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.facet.apigateway.helper.ApiGatewayHelper;
import org.rudi.facet.dataset.bean.InterfaceContract;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataMetadataInfo;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.providers.bean.NodeProvider;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.apigateway.core.bean.Api;
import org.rudi.microservice.apigateway.core.bean.ApiMethod;
import org.rudi.microservice.kalim.service.datafactory.ApiDataFactory;
import org.rudi.microservice.kalim.service.datafactory.KaccessOrganizationDataFactory;
import org.rudi.microservice.kalim.service.datafactory.MediaFileDataFactory;
import org.rudi.microservice.kalim.service.datafactory.MetadataDataFactory;
import org.rudi.microservice.kalim.service.datafactory.MetadataMetadataInfoDataFactory;
import org.rudi.microservice.kalim.service.datafactory.NodeProviderDataFactory;
import org.rudi.microservice.kalim.service.datafactory.OrganizationDataFactory;
import org.rudi.microservice.kalim.service.datafactory.ProviderDataFactory;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiGatewayManagerHelperUT {

	@Mock
	private ApiGatewayHelper apiGatewayHelper;

	@Mock
	private ProviderHelper providerHelper;

	@Mock
	private OrganizationHelper organizationHelper;

	@InjectMocks
	private ApiGatewayManagerHelper apiGatewayManagerHelper;

	private final MediaFileDataFactory mediaFileDataFactory = new MediaFileDataFactory();

	private final MetadataDataFactory metadataDataFactory = new MetadataDataFactory();

	private final KaccessOrganizationDataFactory kaccessOrganizationDataFactory = new KaccessOrganizationDataFactory();

	private final MetadataMetadataInfoDataFactory metadataMetadataInfoDataFactory = new MetadataMetadataInfoDataFactory();

	private final NodeProviderDataFactory nodeProviderDataFactory = new NodeProviderDataFactory();

	private final ProviderDataFactory providerDataFactory = new ProviderDataFactory();

	private final OrganizationDataFactory organizationDataFactory = new OrganizationDataFactory();

	private final ApiDataFactory apiDataFactory = new ApiDataFactory();

	private MediaFile mediaFile(UUID mediaId, String interfaceContractCode) {
		return mediaFileDataFactory.createMediaFile(mediaId, interfaceContractCode);
	}

	private Metadata metadataWithMedias(UUID globalId, UUID producerOrganizationId, List<Media> medias) {
		return metadataDataFactory.createMetadataWithMedias(globalId, producerOrganizationId, medias);
	}

	@Test
	@DisplayName("Création des APIs : chaque média valide du JDD est créé auprès de l'API Gateway avec les infos fournisseur/producteur")
	void test_createApis_nominal() throws Exception {
		final UUID globalId = UUID.randomUUID();
		final UUID mediaId = UUID.randomUUID();
		final UUID nodeProviderId = UUID.randomUUID();
		final UUID providerUuid = UUID.randomUUID();
		final UUID producerOrganizationId = UUID.randomUUID();

		final MediaFile media = mediaFile(mediaId, InterfaceContract.DOWNLOAD.getCode());
		final Metadata metadata = metadataWithMedias(globalId, producerOrganizationId, List.of(media));
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().globalId(globalId)
				.nodeProviderId(nodeProviderId).build();

		final NodeProvider nodeProvider = nodeProviderDataFactory.createNodeProvider(nodeProviderId);
		final Provider provider = providerDataFactory.createProvider(providerUuid);
		final Organization producerOrganization = organizationDataFactory.createOrganizationWithUuid(producerOrganizationId);

		when(providerHelper.requireNodeProviderByUUID(nodeProviderId)).thenReturn(nodeProvider);
		when(providerHelper.requireProviderByNodeProviderUUID(nodeProviderId)).thenReturn(provider);
		when(organizationHelper.getOrganization(producerOrganizationId)).thenReturn(producerOrganization);
		when(apiGatewayHelper.getApiById(globalId, mediaId)).thenReturn(null);
		when(apiGatewayHelper.searchThrottlings(any(), any())).thenReturn(Page.empty());
		when(apiGatewayHelper.createApi(any())).thenAnswer(invocation -> invocation.getArgument(0));

		apiGatewayManagerHelper.createApis(integrationRequest, metadata);

		final ArgumentCaptor<Api> apiCaptor = ArgumentCaptor.forClass(Api.class);
		verify(apiGatewayHelper).createApi(apiCaptor.capture());
		final Api createdApi = apiCaptor.getValue();
		assertThat(createdApi.getGlobalId()).isEqualTo(globalId);
		assertThat(createdApi.getMediaId()).isEqualTo(mediaId);
		assertThat(createdApi.getProviderId()).isEqualTo(providerUuid);
		assertThat(createdApi.getNodeProviderId()).isEqualTo(nodeProviderId);
		assertThat(createdApi.getProducerId()).isEqualTo(producerOrganizationId);
		assertThat(createdApi.getContract()).isEqualTo(InterfaceContract.DOWNLOAD.getCode());
		assertThat(createdApi.getMethods()).containsExactly(ApiMethod.GET);
	}

	@Test
	@DisplayName("Mise à jour des APIs : un média conservé est mis à jour, un média ajouté est créé et un média retiré est supprimé auprès de l'API Gateway")
	void test_updateApis_nominal() throws Exception {
		final UUID globalId = UUID.randomUUID();
		final UUID nodeProviderId = UUID.randomUUID();
		final UUID providerUuid = UUID.randomUUID();
		final UUID producerOrganizationId = UUID.randomUUID();
		final UUID mediaIdModified = UUID.randomUUID();
		final UUID mediaIdAdded = UUID.randomUUID();
		final UUID mediaIdDeleted = UUID.randomUUID();

		final Media mediaModifiedNext = mediaFile(mediaIdModified, InterfaceContract.DOWNLOAD.getCode());
		final Media mediaModifiedPrevious = mediaFile(mediaIdModified, InterfaceContract.DOWNLOAD.getCode());
		final Media mediaAdded = mediaFile(mediaIdAdded, InterfaceContract.DOWNLOAD.getCode());
		final Media mediaDeleted = mediaFile(mediaIdDeleted, InterfaceContract.DOWNLOAD.getCode());

		final Metadata metadata = metadataWithMedias(globalId, producerOrganizationId,
				List.of(mediaModifiedNext, mediaAdded));
		final Metadata actualMetadata = metadataWithMedias(globalId, producerOrganizationId,
				List.of(mediaModifiedPrevious, mediaDeleted));
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().globalId(globalId)
				.nodeProviderId(nodeProviderId).build();

		final NodeProvider nodeProvider = nodeProviderDataFactory.createNodeProvider(nodeProviderId);
		final Provider provider = providerDataFactory.createProvider(providerUuid);
		final Organization producerOrganization = organizationDataFactory.createOrganizationWithUuid(producerOrganizationId);

		when(providerHelper.requireNodeProviderByUUID(nodeProviderId)).thenReturn(nodeProvider);
		when(providerHelper.requireProviderByNodeProviderUUID(nodeProviderId)).thenReturn(provider);
		when(organizationHelper.getOrganization(producerOrganizationId)).thenReturn(producerOrganization);
		when(apiGatewayHelper.searchThrottlings(any(), any())).thenReturn(Page.empty());

		final Api existingApiForModifiedMedia = apiDataFactory.createApiWithId(UUID.randomUUID());
		when(apiGatewayHelper.getApiById(globalId, mediaIdModified)).thenReturn(existingApiForModifiedMedia);
		when(apiGatewayHelper.updateApi(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(apiGatewayHelper.getApiById(globalId, mediaIdAdded)).thenReturn(null);
		when(apiGatewayHelper.createApi(any())).thenAnswer(invocation -> invocation.getArgument(0));

		apiGatewayManagerHelper.updateApis(integrationRequest, metadata, actualMetadata);

		final ArgumentCaptor<Api> updatedApiCaptor = ArgumentCaptor.forClass(Api.class);
		verify(apiGatewayHelper).updateApi(updatedApiCaptor.capture());
		assertThat(updatedApiCaptor.getValue().getMediaId()).isEqualTo(mediaIdModified);

		final ArgumentCaptor<Api> createdApiCaptor = ArgumentCaptor.forClass(Api.class);
		verify(apiGatewayHelper).createApi(createdApiCaptor.capture());
		assertThat(createdApiCaptor.getValue().getMediaId()).isEqualTo(mediaIdAdded);

		verify(apiGatewayHelper).deleteApi(globalId, mediaIdDeleted);
	}

	@Test
	@DisplayName("Suppression des APIs d'une requête d'intégration : toutes les APIs trouvées pour le globalId sont supprimées auprès de l'API Gateway")
	void test_deleteApis_withIntegrationRequest_nominal() throws Exception {
		final UUID globalId = UUID.randomUUID();
		final IntegrationRequestEntity integrationRequest = IntegrationRequestEntity.builder().globalId(globalId)
				.build();

		final Api api1 = apiDataFactory.createApiWithId(UUID.randomUUID());
		final Api api2 = apiDataFactory.createApiWithId(UUID.randomUUID());
		when(apiGatewayHelper.searchApis(any(), eq(Pageable.unpaged())))
				.thenReturn(new PageImpl<>(List.of(api1, api2)));

		apiGatewayManagerHelper.deleteApis(integrationRequest);

		verify(apiGatewayHelper).deleteApi(api1.getApiId());
		verify(apiGatewayHelper).deleteApi(api2.getApiId());
	}

	@Test
	@DisplayName("Suppression de toutes les APIs : aucune suppression n'est déclenchée si l'API Gateway ne renvoie aucune page de résultats")
	void test_deleteApis_noArgs_nominal() throws Exception {
		when(apiGatewayHelper.searchApis(any(), eq(Pageable.unpaged()))).thenReturn(null);

		apiGatewayManagerHelper.deleteApis();

		verify(apiGatewayHelper, never()).deleteApi(any(UUID.class));
	}

	@Test
	@DisplayName("Récupération des médias valides : seuls les médias ayant un format d'interface reconnu (template OpenAPI) sont conservés")
	void test_getValidMedias_nominal() {
		final Media validMedia = mediaFile(UUID.randomUUID(), InterfaceContract.DOWNLOAD.getCode());
		final Media invalidMedia = mediaFile(UUID.randomUUID(), "unknown-contract-code");
		final Metadata metadata = metadataWithMedias(UUID.randomUUID(), UUID.randomUUID(),
				List.of(validMedia, invalidMedia));

		final List<Media> validMedias = apiGatewayManagerHelper.getValidMedias(metadata);

		assertThat(validMedias).containsExactly(validMedia);
	}

	@Test
	@DisplayName("Synchronisation des médias d'un JDD : les APIs des médias actuels sont créées/conservées et les APIs orphelines sont supprimées")
	void test_synchronizeMedias_nominal() throws Exception {
		final UUID globalId = UUID.randomUUID();
		final UUID mediaId = UUID.randomUUID();
		final UUID metadataProviderOrganizationId = UUID.randomUUID();
		final UUID producerOrganizationId = UUID.randomUUID();
		final UUID providerUuid = UUID.randomUUID();
		final UUID nodeProviderUuid = UUID.randomUUID();
		final UUID orphanApiId = UUID.randomUUID();
		final UUID orphanMediaId = UUID.randomUUID();

		final MediaFile media = mediaFile(mediaId, InterfaceContract.DOWNLOAD.getCode());
		final Metadata metadata = metadataWithMedias(globalId, producerOrganizationId, List.of(media));

		final org.rudi.facet.kaccess.bean.Organization metadataProviderOrganization = kaccessOrganizationDataFactory
				.createOrganizationWithId(metadataProviderOrganizationId);
		final MetadataMetadataInfo metadataInfo = metadataMetadataInfoDataFactory
				.createMetadataMetadataInfoWithProvider(metadataProviderOrganization);
		metadata.setMetadataInfo(metadataInfo);

		final NodeProvider nodeProvider = nodeProviderDataFactory.createNodeProvider(nodeProviderUuid);
		final Provider provider = providerDataFactory.createProviderWithNodeProvider(providerUuid, nodeProvider);
		final Organization producerOrganization = organizationDataFactory.createOrganizationWithUuid(producerOrganizationId);

		when(providerHelper.getProviderByUUID(metadataProviderOrganizationId)).thenReturn(provider);
		when(organizationHelper.getOrganization(producerOrganizationId)).thenReturn(producerOrganization);
		when(apiGatewayHelper.getApiById(globalId, mediaId)).thenReturn(null);
		when(apiGatewayHelper.createApi(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(apiGatewayHelper.searchThrottlings(any(), any())).thenReturn(Page.empty());

		final Api keptApi = apiDataFactory.createApi(UUID.randomUUID(), globalId, mediaId);
		final Api orphanApi = apiDataFactory.createApi(orphanApiId, globalId, orphanMediaId);
		when(apiGatewayHelper.searchApis(any(), eq(Pageable.unpaged())))
				.thenReturn(new PageImpl<>(List.of(keptApi, orphanApi)));

		final List<Api> handledApis = apiGatewayManagerHelper.synchronizeMedias(metadata);

		assertThat(handledApis).hasSize(1);
		assertThat(handledApis.get(0).getMediaId()).isEqualTo(mediaId);
		assertThat(handledApis.get(0).getNodeProviderId()).isEqualTo(nodeProviderUuid);
		assertThat(handledApis.get(0).getProviderId()).isEqualTo(providerUuid);
		verify(apiGatewayHelper).deleteApi(orphanApiId);
		verify(apiGatewayHelper, never()).deleteApi(keptApi.getApiId());
	}

}
