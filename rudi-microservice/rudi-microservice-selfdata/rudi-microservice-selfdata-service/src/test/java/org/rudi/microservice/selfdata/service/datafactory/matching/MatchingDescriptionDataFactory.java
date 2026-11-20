package org.rudi.microservice.selfdata.service.datafactory.matching;

import org.rudi.microservice.selfdata.core.bean.MatchingDescription;
import org.springframework.stereotype.Component;

@Component
public class MatchingDescriptionDataFactory {

	public MatchingDescription create(String token) {
		return new MatchingDescription().token(token);
	}

}
