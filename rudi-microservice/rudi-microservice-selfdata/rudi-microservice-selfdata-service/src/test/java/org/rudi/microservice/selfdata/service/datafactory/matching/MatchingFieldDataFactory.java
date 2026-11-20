package org.rudi.microservice.selfdata.service.datafactory.matching;

import org.rudi.microservice.selfdata.core.bean.MatchingField;
import org.springframework.stereotype.Component;

@Component
public class MatchingFieldDataFactory {

	public MatchingField create() {
		return new MatchingField();
	}

}
