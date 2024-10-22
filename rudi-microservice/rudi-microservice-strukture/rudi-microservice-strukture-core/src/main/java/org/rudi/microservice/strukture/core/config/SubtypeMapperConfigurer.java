package org.rudi.microservice.strukture.core.config;


import java.util.List;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rudi.common.core.json.ObjectMapperUtils;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubtypeMapperConfigurer {

	private final ObjectMapper objectMapper;

	@PostConstruct
	public void addSubTypes() {
		objectMapper.registerSubtypes(ObjectMapperUtils.namedTypesWithSimpleNames(List.of(
				Organization.class,
				LinkedProducer.class
		)));
	}
}
