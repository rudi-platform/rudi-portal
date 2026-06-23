package org.rudi.microservice.projekt.service.datafactory;

import java.time.LocalDateTime;

import org.rudi.common.service.datafactory.AbstractStampedDataFactory;
import org.rudi.microservice.projekt.storage.dao.reutilisationstatus.ReutilisationStatusDao;
import org.rudi.microservice.projekt.storage.entity.ReutilisationStatusEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ReutilisationStatusDataFactory
		extends AbstractStampedDataFactory<ReutilisationStatusEntity, ReutilisationStatusDao> {

	public ReutilisationStatusDataFactory(ReutilisationStatusDao repository) {
		super(repository, ReutilisationStatusEntity.class);
	}

	public ReutilisationStatusEntity getOrCreateProjectStatus() {
		return getOrCreate("PROJECT", "Reutilisation en projet (datafactory)", 1,
				LocalDateTime.now().minusYears(1), null);
	}
}

