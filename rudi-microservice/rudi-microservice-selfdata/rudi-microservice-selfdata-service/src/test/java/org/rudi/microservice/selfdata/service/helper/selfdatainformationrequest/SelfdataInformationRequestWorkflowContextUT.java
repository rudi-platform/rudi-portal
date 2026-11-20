package org.rudi.microservice.selfdata.service.helper.selfdatainformationrequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.Test;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.service.TaskConstants;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.facet.kaccess.bean.MatchingData;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.Organization;
import org.rudi.facet.kaccess.bean.SelfdataContent;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.bean.OrganizationMember;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.microservice.selfdata.core.bean.MatchingDescription;
import org.rudi.microservice.selfdata.core.bean.MatchingField;
import org.rudi.microservice.selfdata.core.bean.NodeProviderInfo;
import org.rudi.microservice.selfdata.service.SelfdataSpringBootTest;
import org.rudi.microservice.selfdata.service.datafactory.OrganizationMemberDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.UserDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.kaccess.MatchingDataDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.kaccess.MetadataDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.kaccess.OrganizationDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.matching.MatchingDescriptionDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.matching.MatchingFieldDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.matching.NodeProviderInfoDataFactory;
import org.rudi.microservice.selfdata.service.datafactory.selfdatainformationrequest.SelfdataInformationRequestDataFactory;
import org.rudi.microservice.selfdata.service.helper.provider.SelfdataProviderHelper;
import org.rudi.microservice.selfdata.service.helper.selfdatamatchingdata.SelfdataMatchingDataHelper;
import org.rudi.microservice.selfdata.service.utils.SelfdataPairingUtils;
import org.rudi.microservice.selfdata.storage.dao.selfdatainformationrequest.SelfdataInformationRequestDao;
import org.rudi.microservice.selfdata.storage.entity.selfdatainformationrequest.SelfdataInformationRequestEntity;
import org.rudi.microservice.selfdata.storage.entity.selfdatainformationrequest.SelfdataInformationRequestStatus;
import org.rudi.microservice.selfdata.storage.entity.selfdatainformationrequest.SelfdataTokenTupleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SelfdataSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
class SelfdataInformationRequestWorkflowContextUT {

	private final SelfdataInformationRequestWorkflowContext workflowContext;
	private final SelfdataInformationRequestDao selfdataInformationRequestDao;
	private final SelfdataInformationRequestDataFactory selfdataInformationRequestDataFactory;
	private final UserDataFactory userDataFactory;
	private final OrganizationMemberDataFactory organizationMemberDataFactory;
	private final OrganizationDataFactory organizationDataFactory;
	private final MetadataDataFactory metadataDataFactory;
	private final MatchingDataDataFactory matchingDataDataFactory;
	private final MatchingFieldDataFactory matchingFieldDataFactory;
	private final NodeProviderInfoDataFactory nodeProviderInfoDataFactory;
	private final MatchingDescriptionDataFactory matchingDescriptionDataFactory;

	@MockitoBean
	private EMailService eMailService;
	@MockitoBean
	private TemplateGenerator templateGenerator;
	@MockitoBean
	private SelfdataInformationRequestAssigmentHelper assignmentHelper;
	@MockitoBean
	private ACLHelper aclHelper;
	@MockitoBean
	private FormHelper formHelper;
	@MockitoBean
	private DatasetService datasetService;
	@MockitoBean
	private OrganizationHelper organizationHelper;
	@MockitoBean
	private SelfdataProviderHelper selfdataProviderHelper;
	@MockitoBean
	private SelfdataPairingUtils selfdataPairingUtils;
	@MockitoBean
	private SelfdataMatchingDataHelper selfdataMatchingDataHelper;

	private ExecutionEntity mockExecutionEntity(UUID uuid) {
		final ExecutionEntity executionEntity = mock(ExecutionEntity.class);
		lenient().when(executionEntity.getProcessInstanceBusinessKey()).thenReturn(uuid.toString());
		return executionEntity;
	}

	@Test
	void test_updateStatus_nominal() {
		final SelfdataInformationRequestEntity entity = selfdataInformationRequestDataFactory.getOrCreate(null, null);
		final ExecutionEntity executionEntity = mockExecutionEntity(entity.getUuid());

		workflowContext.updateStatus(null, executionEntity, Status.COMPLETED.name(),
				SelfdataInformationRequestStatus.VALIDATED.name(), "DONE");

		assertThat(entity.getStatus()).isEqualTo(Status.COMPLETED);
		assertThat(entity.getSelfdataInformationRequestStatus()).isEqualTo(SelfdataInformationRequestStatus.VALIDATED);
		assertThat(entity.getFunctionalStatus()).isEqualTo("DONE");
		verify(executionEntity).setVariable(TaskConstants.FUNCTIONAL_STATUS, "DONE");
	}

	@Test
	void test_computeModerators_nominal() {
		final SelfdataInformationRequestEntity entity = selfdataInformationRequestDataFactory.getOrCreate(null, null);
		final ExecutionEntity executionEntity = mockExecutionEntity(entity.getUuid());
		final User moderator = userDataFactory.createSimpleUser(null, "moderator1");
		when(aclHelper.searchUsers("MODERATOR")).thenReturn(List.of(moderator));
		when(aclHelper.lookupEMailAddress(moderator)).thenReturn("moderator1@rudi.fr");

		final List<String> result = workflowContext.computeModerators(null, executionEntity, null, null);

		assertThat(result).containsExactly("MODERATOR");
		verify(aclHelper).searchUsers("MODERATOR");
		verifyNoInteractions(eMailService);
	}

	@Test
	void test_computePotentialProducersOwners_4args_nominal() throws Exception {
		final UUID datasetUuid = UUID.randomUUID();
		final SelfdataInformationRequestEntity entity = selfdataInformationRequestDataFactory.getOrCreate(null,
				datasetUuid);
		final ExecutionEntity executionEntity = mockExecutionEntity(entity.getUuid());

		final UUID organizationUuid = UUID.randomUUID();
		final Organization organization = organizationDataFactory.create(organizationUuid, "Organization1");
		final Metadata metadata = metadataDataFactory.createWithProducer(organization);
		when(datasetService.getDataset(datasetUuid)).thenReturn(metadata);

		final UUID memberUserUuid = UUID.randomUUID();
		final OrganizationMember member = organizationMemberDataFactory.create(memberUserUuid);
		when(organizationHelper.getOrganizationMembers(organizationUuid)).thenReturn(List.of(member));

		final User user = userDataFactory.createSimpleUser(memberUserUuid, "producer1");
		when(aclHelper.getUserByUUID(memberUserUuid)).thenReturn(user);
		lenient().when(aclHelper.lookupEMailAddress(user)).thenReturn("producer1@rudi.fr");

		final List<String> result = workflowContext.computePotentialProducersOwners(null, executionEntity, null, null);

		assertThat(result).containsExactly("producer1");
		verifyNoInteractions(eMailService);
	}

	@Test
	void test_computePotentialProducersOwners_5args_nominal() throws Exception {
		final UUID datasetUuid = UUID.randomUUID();
		final SelfdataInformationRequestEntity entity = selfdataInformationRequestDataFactory.getOrCreate(null,
				datasetUuid);
		final ExecutionEntity executionEntity = mockExecutionEntity(entity.getUuid());

		final UUID organizationUuid = UUID.randomUUID();
		final Organization organization = organizationDataFactory.create(organizationUuid, "Organization1");
		final Metadata metadata = metadataDataFactory.createWithProducer(organization);
		when(datasetService.getDataset(datasetUuid)).thenReturn(metadata);

		final UUID memberUserUuid = UUID.randomUUID();
		final OrganizationMember member = organizationMemberDataFactory.create(memberUserUuid);
		when(organizationHelper.getOrganizationMembers(organizationUuid)).thenReturn(List.of(member));

		final User user = userDataFactory.createSimpleUser(memberUserUuid, "producer1");
		when(aclHelper.getUserByUUID(memberUserUuid)).thenReturn(user);
		lenient().when(aclHelper.lookupEMailAddress(user)).thenReturn("producer1@rudi.fr");

		final List<String> result = workflowContext.computePotentialProducersOwners(null, executionEntity, "Subject",
				"Body", true);

		assertThat(result).containsExactly("producer1");
		verifyNoInteractions(eMailService);
	}

	@Test
	void test_getAccessType_nominal() throws DataverseAPIException {
		final UUID datasetUuid = UUID.randomUUID();
		final SelfdataInformationRequestEntity entity = selfdataInformationRequestDataFactory.getOrCreate(null,
				datasetUuid);
		final ExecutionEntity executionEntity = mockExecutionEntity(entity.getUuid());

		final Metadata metadata = metadataDataFactory.createWithSelfdataAccess(SelfdataContent.SelfdataAccessEnum.API);
		when(datasetService.getDataset(datasetUuid)).thenReturn(metadata);

		final String result = workflowContext.getAccessType(null, executionEntity);

		assertThat(result).isEqualTo("API");
	}

	@Test
	void test_getInitiator_nominal() {
		final SelfdataInformationRequestEntity entity = selfdataInformationRequestDataFactory.getOrCreate(null, null);
		entity.setInitiator("citizen1");
		selfdataInformationRequestDao.save(entity);
		final ExecutionEntity executionEntity = mockExecutionEntity(entity.getUuid());
		final User user = userDataFactory.createSimpleUser(null, "citizen1");
		when(aclHelper.getUserByLogin("citizen1")).thenReturn(user);
		lenient().when(aclHelper.lookupEMailAddress(user)).thenReturn("citizen1@rudi.fr");

		final List<String> result = workflowContext.getInitiator(null, executionEntity, null, null);

		assertThat(result).containsExactly("citizen1");
		verifyNoInteractions(eMailService);
	}

	@Test
	void test_callMatchingDataService_nominal() throws Exception {
		final UUID datasetUuid = UUID.randomUUID();
		final SelfdataInformationRequestEntity entity = selfdataInformationRequestDataFactory.getOrCreate(null,
				datasetUuid);
		entity.setInitiator("citizen1");
		entity.setData("{}");
		selfdataInformationRequestDao.save(entity);
		final ExecutionEntity executionEntity = mockExecutionEntity(entity.getUuid());

		final UUID citizenUuid = UUID.randomUUID();
		final User user = userDataFactory.createSimpleUser(citizenUuid, "citizen1");
		when(aclHelper.getUserByLogin("citizen1")).thenReturn(user);

		final Map<String, Object> formData = new HashMap<>();
		when(formHelper.hydrateData("{}")).thenReturn(formData);

		final Metadata metadata = metadataDataFactory.createEmpty();
		when(datasetService.getDataset(datasetUuid)).thenReturn(metadata);

		final List<MatchingData> matchingDataList = List.of(matchingDataDataFactory.create());
		when(selfdataPairingUtils.getMatchingDataList(metadata)).thenReturn(matchingDataList);

		final List<MatchingField> matchingFields = List.of(matchingFieldDataFactory.create());
		when(selfdataPairingUtils.extractMatchingDataFromFormData(formData, matchingDataList))
				.thenReturn(matchingFields);

		final UUID providerUuid = UUID.randomUUID();
		final NodeProviderInfo nodeProviderInfo = nodeProviderInfoDataFactory.create("http://provider", providerUuid);
		when(selfdataPairingUtils.getNodeProviderInfo(metadata)).thenReturn(nodeProviderInfo);

		final UUID token = UUID.randomUUID();
		final MatchingDescription matchingDescription = matchingDescriptionDataFactory.create(token.toString());
		when(selfdataProviderHelper.sendMatchingDataForPairing("http://provider", datasetUuid, "citizen1",
				matchingFields)).thenReturn(matchingDescription);

		final SelfdataTokenTupleEntity tokenTupleEntity = new SelfdataTokenTupleEntity();
		tokenTupleEntity.setToken(token);
		when(selfdataPairingUtils.savePairingTokenForUser(token, citizenUuid, datasetUuid, providerUuid))
				.thenReturn(tokenTupleEntity);

		when(formHelper.deshydrateData(formData)).thenReturn("{}");

		final String result = workflowContext.callMatchingDataService(null, executionEntity);

		assertThat(result).isEqualTo(token.toString());
		assertThat(entity.getUserPresent()).isTrue();
		verify(selfdataMatchingDataHelper).decrypt(matchingFields);
	}

}
