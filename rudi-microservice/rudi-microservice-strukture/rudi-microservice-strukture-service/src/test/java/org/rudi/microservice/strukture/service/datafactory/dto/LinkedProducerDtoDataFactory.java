package org.rudi.microservice.strukture.service.datafactory.dto;

import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.springframework.stereotype.Component;

@Component
public class LinkedProducerDtoDataFactory {

	public LinkedProducer create() {
		return new LinkedProducer();
	}

}
