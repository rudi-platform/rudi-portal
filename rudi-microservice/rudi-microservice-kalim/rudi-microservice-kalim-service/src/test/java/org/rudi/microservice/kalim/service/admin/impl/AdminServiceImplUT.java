package org.rudi.microservice.kalim.service.admin.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.datafactory.RoleDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.apigateway.exceptions.DeleteApiException;
import org.rudi.facet.dataverse.api.dataset.DatasetOperationAPI;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.dataverse.bean.Dataset;
import org.rudi.facet.dataverse.bean.DatasetVersion;
import org.rudi.facet.dataverse.model.search.SearchParams;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataListFacets;
import org.rudi.facet.kaccess.helper.dataset.metadatablock.MetadataBlockHelper;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.bean.OrganizationMember;
import org.rudi.facet.organization.bean.OrganizationRole;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.AddUserToOrganizationException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.core.bean.OrganizationsReparationReport;
import org.rudi.microservice.kalim.service.datafactory.ApiDataFactory;
import org.rudi.microservice.kalim.service.datafactory.DatasetDataFactory;
import org.rudi.microservice.kalim.service.datafactory.DatasetMetadataBlockDataFactory;
import org.rudi.microservice.kalim.service.datafactory.DatasetVersionDataFactory;
import org.rudi.microservice.kalim.service.datafactory.MediaDataFactory;
import org.rudi.microservice.kalim.service.datafactory.MetadataDataFactory;
import org.rudi.microservice.kalim.service.datafactory.MetadataListDataFactory;
import org.rudi.microservice.kalim.service.datafactory.MetadataListFacetsDataFactory;
import org.rudi.microservice.kalim.service.datafactory.OrganizationDataFactory;
import org.rudi.microservice.kalim.service.datafactory.SearchDatasetInfoDataFactory;
import org.rudi.microservice.kalim.service.datafactory.SearchElementsDataFactory;
import org.rudi.microservice.kalim.service.helper.apigateway.ApiGatewayManagerHelper;
import org.rudi.microservice.kalim.service.integration.IntegrationRequestService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplUT {

	private static final String DEFAULT_ORGANIZATION_PASSWORD = "Test1234$";

	@Mock
	private DatasetOperationAPI datasetOperationAPI;
	@Mock
	private ResourceRepairer resourceRepairer;
	@Mock
	private MetadataBlockHelper metadataBLockHelper;
	@Mock
	private DatasetService datasetService;
	@Mock
	private ApiGatewayManagerHelper apiGatewayManagerHelper;
	@Mock
	private ProviderHelper providerHelper;
	@Mock
	private IntegrationRequestService integrationRequestService;
	@Mock
	private UtilPageable utilPageable;
	@Mock
	private OrganizationHelper organizationHelper;
	@Mock
	private ACLHelper aclHelper;

	private AdminServiceImpl adminService;

	private RoleDataFactory roleDataFactory;

	private final DatasetVersionDataFactory datasetVersionDataFactory = new DatasetVersionDataFactory();

	private final DatasetDataFactory datasetDataFactory = new DatasetDataFactory();

	private final DatasetMetadataBlockDataFactory datasetMetadataBlockDataFactory = new DatasetMetadataBlockDataFactory();

	private final MediaDataFactory mediaDataFactory = new MediaDataFactory();

	private final MetadataDataFactory metadataDataFactory = new MetadataDataFactory();

	private final SearchDatasetInfoDataFactory searchDatasetInfoDataFactory = new SearchDatasetInfoDataFactory();

	private final SearchElementsDataFactory searchElementsDataFactory = new SearchElementsDataFactory();

	private final OrganizationDataFactory organizationDataFactory = new OrganizationDataFactory();

	private final ApiDataFactory apiDataFactory = new ApiDataFactory();

	private final MetadataListDataFactory metadataListDataFactory = new MetadataListDataFactory();

	private final MetadataListFacetsDataFactory metadataListFacetsDataFactory = new MetadataListFacetsDataFactory();

	@BeforeEach
	void setUp() {
		adminService = new AdminServiceImpl(datasetOperationAPI, List.of(resourceRepairer), metadataBLockHelper,
				datasetService, apiGatewayManagerHelper, providerHelper, integrationRequestService, utilPageable,
				organizationHelper, aclHelper);
		ReflectionTestUtils.setField(adminService, "rudiAlias", "rudi_test");
		ReflectionTestUtils.setField(adminService, "defaultOrganizationRoles", List.of("USER", "ORGANIZATION"));
		ReflectionTestUtils.setField(adminService, "defaultOrganizationPassword", DEFAULT_ORGANIZATION_PASSWORD);
		roleDataFactory = new RoleDataFactory(aclHelper);
	}

	@Test
	@DisplayName("Réparation des ressources : la ressource réparée par le repairer est mise à jour avec des dates de média corrigées")
	void test_repairResources_nominal() throws DataverseAPIException {
		final String persistentId = "persistent-id-1";

		final DatasetVersion datasetVersion = datasetVersionDataFactory.createDatasetVersion(1L, 2L, persistentId,
				"RELEASED", 1, 0, datasetMetadataBlockDataFactory.createEmptyDatasetMetadataBlock(),
				Collections.emptyList());

		final Dataset dataset = datasetDataFactory.createDatasetWithLatestVersion(datasetVersion);

		final Media mediaWithoutDates = mediaDataFactory.createEmptyMedia();

		final Metadata metadata = metadataDataFactory.createMetadataWithMedias(UUID.randomUUID(),
				List.of(mediaWithoutDates));

		when(resourceRepairer.getQuery()).thenReturn("query1");
		when(datasetOperationAPI.searchDataset(any(SearchParams.class))).thenReturn(searchElementsDataFactory
				.createSearchDatasetElements(1L,
						List.of(searchDatasetInfoDataFactory.createSearchDatasetInfoWithGlobalId(persistentId))));
		when(datasetOperationAPI.getDataset(persistentId)).thenReturn(dataset);
		when(metadataBLockHelper.datasetMetadataBlockToMetadata(datasetVersion.getMetadataBlocks(), persistentId))
				.thenReturn(metadata);
		when(metadataBLockHelper.metadataToDatasetMetadataBlock(metadata))
				.thenReturn(datasetMetadataBlockDataFactory.createEmptyDatasetMetadataBlock());

		adminService.repairResources();

		verify(resourceRepairer, times(1)).repairResource(datasetVersion);
		assertThat(mediaWithoutDates.getMediaDates()).isNotNull();
		assertThat(mediaWithoutDates.getMediaDates().getCreated()).isNotNull();
		assertThat(mediaWithoutDates.getMediaDates().getUpdated()).isNotNull();

		final ArgumentCaptor<DatasetVersion> updatedVersionCaptor = ArgumentCaptor.forClass(DatasetVersion.class);
		verify(datasetOperationAPI, times(1)).updateDataset(updatedVersionCaptor.capture(), eq(persistentId));
		assertThat(updatedVersionCaptor.getValue().getDatasetPersistentId()).isEqualTo(persistentId);
	}

	@Test
	@DisplayName("Réparation des organisations : un utilisateur ROBOT est créé et ajouté comme EDITOR pour chaque organisation sans utilisateur")
	void test_repairOrganizations_nominal()
			throws AppServiceException, GetOrganizationException, AddUserToOrganizationException {
		final UUID organizationUuid = UUID.randomUUID();
		final Organization organization = organizationDataFactory.createOrganizationWithUuid(organizationUuid);

		when(organizationHelper.getAllOrganizations()).thenReturn(List.of(organization));
		when(aclHelper.getUserByLogin(organizationUuid.toString())).thenReturn(null);
		when(aclHelper.searchRoles()).thenReturn(List.of(roleDataFactory.createRole("USER"),
				roleDataFactory.createRole("ORGANIZATION"), roleDataFactory.createRole("ADMIN")));
		when(aclHelper.createUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		final OrganizationsReparationReport report = adminService.repairOrganizations();

		assertThat(report.getOrganizationsRepaired()).containsExactly(organizationUuid);
		assertThat(report.getPasswordUsed()).isEqualTo(DEFAULT_ORGANIZATION_PASSWORD);

		final ArgumentCaptor<User> createdUserCaptor = ArgumentCaptor.forClass(User.class);
		verify(aclHelper, times(1)).createUser(createdUserCaptor.capture());
		final User createdUser = createdUserCaptor.getValue();
		assertThat(createdUser.getLogin()).isEqualTo(organizationUuid.toString());
		assertThat(createdUser.getPassword()).isEqualTo(DEFAULT_ORGANIZATION_PASSWORD);
		assertThat(createdUser.getType()).isEqualTo(UserType.ROBOT);
		assertThat(createdUser.getRoles()).extracting(Role::getCode).containsExactlyInAnyOrder("USER", "ORGANIZATION");

		final ArgumentCaptor<OrganizationMember> memberCaptor = ArgumentCaptor.forClass(OrganizationMember.class);
		verify(organizationHelper, times(1)).addMemberToOrganization(memberCaptor.capture(), eq(organizationUuid));
		assertThat(memberCaptor.getValue().getRole()).isEqualTo(OrganizationRole.EDITOR);
	}

	@Test
	@DisplayName("Création des APIs manquantes : les médias d'un jeu de données sont synchronisés avec l'API gateway")
	void test_createMissingApis_nominal() throws DataverseAPIException {
		final UUID globalId = UUID.randomUUID();
		final Metadata metadataWithMedia = metadataDataFactory.createMetadataWithMedias(globalId,
				List.of(mediaDataFactory.createEmptyMedia()));

		final MetadataListFacets firstPage = metadataListFacetsDataFactory
				.createMetadataListFacets(metadataListDataFactory.createMetadataList(0L, List.of(metadataWithMedia)));
		final MetadataListFacets lastPage = metadataListFacetsDataFactory.createMetadataListFacets(
				metadataListDataFactory.createMetadataList(1L, Collections.emptyList()));

		when(datasetService.searchDatasets(any(), eq(Collections.emptyList()))).thenReturn(firstPage, lastPage);
		when(apiGatewayManagerHelper.synchronizeMedias(metadataWithMedia))
				.thenReturn(List.of(apiDataFactory.createApi()));

		adminService.createMissingApis(List.of(globalId));

		verify(datasetService, times(2)).searchDatasets(any(), eq(Collections.emptyList()));
		verify(apiGatewayManagerHelper, times(1)).synchronizeMedias(metadataWithMedia);
	}

	@Test
	@DisplayName("Suppression de toutes les APIs : l'API gateway est sollicitée pour supprimer l'ensemble des APIs")
	void test_deleteAllApis_nominal() throws DeleteApiException {
		adminService.deleteAllApis();

		verify(apiGatewayManagerHelper, times(1)).deleteApis();
		verify(resourceRepairer, never()).repairResource(any());
	}
}
