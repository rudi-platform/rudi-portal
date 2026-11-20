package org.rudi.microservice.projekt.service.datafactory;

import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.ComputeIndicatorsSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class ComputeIndicatorsSearchCriteriaDataFactory {

	public ComputeIndicatorsSearchCriteria createWithProjectUuid(UUID projectUuid) {
		return new ComputeIndicatorsSearchCriteria(projectUuid);
	}

}
