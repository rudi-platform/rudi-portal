package org.rudi.microservice.selfdata.service.datafactory.kaccess;

import org.rudi.facet.kaccess.bean.MatchingData;
import org.springframework.stereotype.Component;

@Component
public class MatchingDataDataFactory {

	public MatchingData create() {
		return new MatchingData();
	}

}
