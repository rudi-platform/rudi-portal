package org.rudi.microservice.projekt.service.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.facet.acl.bean.ProjectKeystore;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.ProjectKeystoreSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus.ARCHIVED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectHelper {
	private static final String PROJECT_ARCHIVED_FUNCTIONAL_STATUS = "Archivé";
	private static final String PROJECT_DISENGAGED_FUNCTIONAL_STATUS = "Archivé - Non accessible au grand public";
	private static final String LINKED_DATASET_ARCHIVED_FUNCTIONAL_STATUS = "Archivé";

	private final ProjectDao projectDao;
	private final UtilPageable utilPageable;
	private final ACLHelper aclHelper;

	@Transactional
	public void archiveProject(ProjectEntity project, ProjectStatus projectStatus, LinkedDatasetStatus linkedDatasetStatus){
		if(project == null || !ProjectStatus.VALIDATED.equals(project.getProjectStatus())){
			throw new IllegalStateException("Projet non valide");
		}

		project.setProjectStatus(projectStatus);
		project.setStatus(Status.DELETED);
		project.setFunctionalStatus(ProjectStatus.DISENGAGED.equals(projectStatus) ? PROJECT_DISENGAGED_FUNCTIONAL_STATUS : PROJECT_ARCHIVED_FUNCTIONAL_STATUS);
		project.setUpdatedDate(LocalDateTime.now());

		handleArchivedLinkedDataset(project, linkedDatasetStatus);

		handleArchivedNewDatasetRequest(project);

		projectDao.save(project);
	}

	private void handleArchivedLinkedDataset(ProjectEntity project, LinkedDatasetStatus linkedDatasetStatus){
		for(LinkedDatasetEntity ld : project.getLinkedDatasets()){
			ld.setLinkedDatasetStatus(linkedDatasetStatus);
			ld.setStatus(Status.DELETED);
			ld.setFunctionalStatus(LINKED_DATASET_ARCHIVED_FUNCTIONAL_STATUS);
			ld.setUpdatedDate(LocalDateTime.now());

		}
	}

	private void handleArchivedNewDatasetRequest(ProjectEntity project){
		for (NewDatasetRequestEntity newDatasetRequestEntity : project.getDatasetRequests()) {
			newDatasetRequestEntity.setNewDatasetRequestStatus(ARCHIVED);
			newDatasetRequestEntity.setStatus(Status.DELETED);
			newDatasetRequestEntity.setFunctionalStatus(LINKED_DATASET_ARCHIVED_FUNCTIONAL_STATUS);
			newDatasetRequestEntity.setUpdatedDate(LocalDateTime.now());
		}
	}

	public void deleteProjectKeyStore(ProjectEntity project){
		ProjectKeystoreSearchCriteria searchCriteria = new ProjectKeystoreSearchCriteria();
		List<UUID> uuidList = new ArrayList<>();
		uuidList.add(project.getUuid());
		searchCriteria.setProjectUuids(uuidList);
		Pageable pageable = utilPageable.getPageable(0,1,null);
		Optional<ProjectKeystore> projectKeystore = aclHelper.searchProjectKeystores(searchCriteria, pageable).get().findFirst();
		if (projectKeystore.isPresent()) {
			aclHelper.deleteProjectKeyStore(projectKeystore.get().getUuid());
		} else {
			log.info("There is no projectKeystore associated to the project {}.", project.getUuid());
		}
	}
}
