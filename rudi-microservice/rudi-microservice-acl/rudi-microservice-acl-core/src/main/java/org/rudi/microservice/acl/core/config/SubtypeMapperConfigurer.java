package org.rudi.microservice.acl.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.annotation.PostConstruct;
import org.rudi.microservice.acl.core.bean.ExternalAccountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubtypeMapperConfigurer {

	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	public void addSubTypes() {
		objectMapper.registerSubtypes(new NamedType(ExternalAccountRequest.class,
				ExternalAccountRequest.class.getSimpleName()));
	}

}
