package org.rudi.microservice.selfdata.service.datafactory.selfdatainformationrequest;

import java.time.LocalDateTime;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.selfdata.storage.dao.selfdatainformationrequest.SelfdataInformationRequestDao;
import org.rudi.microservice.selfdata.storage.entity.selfdatainformationrequest.SelfdataInformationRequestEntity;
import org.rudi.microservice.selfdata.storage.entity.selfdatainformationrequest.SelfdataInformationRequestStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class SelfdataInformationRequestDataFactory extends
		AbstractAssetDescriptionDataFactory<SelfdataInformationRequestEntity, SelfdataInformationRequestDao> {

	public static final String PROCESS_DEFINITION_KEY = "selfdata-information-request-process";

	public SelfdataInformationRequestDataFactory(SelfdataInformationRequestDao repository) {
		super(repository, SelfdataInformationRequestEntity.class);
	}

	private UUID pendingDatasetUuid;

	@Override
	protected void assignData(SelfdataInformationRequestEntity item) {
		item.setUpdatedDate(handleDate(LocalDateTime.now()));
		item.setDatasetUuid(pendingDatasetUuid);
	}

	public SelfdataInformationRequestEntity getOrCreate(UUID uuid, UUID datasetUuid) {
		if (uuid == null) {
			uuid = UUID.randomUUID();
		} else {
			SelfdataInformationRequestEntity entity = repository.findByUuid(uuid);
			if (entity != null) {
				return entity;
			}
		}
		pendingDatasetUuid = datasetUuid != null ? datasetUuid : UUID.randomUUID();
		SelfdataInformationRequestEntity entity = create(uuid, PROCESS_DEFINITION_KEY, Status.DRAFT, "Créée",
				randomString(20), LocalDateTime.now(), randomString(60));
		entity.setSelfdataInformationRequestStatus(SelfdataInformationRequestStatus.DRAFT);
		return repository.save(entity);
	}

}
