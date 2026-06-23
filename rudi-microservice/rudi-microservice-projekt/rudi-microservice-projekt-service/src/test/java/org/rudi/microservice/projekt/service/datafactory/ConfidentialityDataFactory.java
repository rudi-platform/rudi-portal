package org.rudi.microservice.projekt.service.datafactory;

import java.time.LocalDateTime;

import org.rudi.common.service.datafactory.AbstractStampedDataFactory;
import org.rudi.microservice.projekt.storage.dao.confidentiality.ConfidentialityDao;
import org.rudi.microservice.projekt.storage.entity.ConfidentialityEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ConfidentialityDataFactory extends AbstractStampedDataFactory<ConfidentialityEntity, ConfidentialityDao> {

	private static final String DEFAULT_CONFIDENTIALITY_CODE = "datafactory-public";

	public ConfidentialityDataFactory(ConfidentialityDao repository) {
		super(repository, ConfidentialityEntity.class);
	}

	@Override
	protected void assignData(ConfidentialityEntity item) {
		item.setPrivateAccess(false);
		item.setDescription("Confidentialite creee par la datafactory");
	}

	public ConfidentialityEntity getOrCreatePublicConfidentiality() {
		return getOrCreate(DEFAULT_CONFIDENTIALITY_CODE, "Public (datafactory)", 1,
				LocalDateTime.now().minusYears(1), null);
	}
}

