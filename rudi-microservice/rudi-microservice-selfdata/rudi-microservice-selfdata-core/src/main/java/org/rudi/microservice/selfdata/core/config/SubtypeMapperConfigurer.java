package org.rudi.microservice.selfdata.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.annotation.PostConstruct;
import org.rudi.microservice.selfdata.core.bean.SelfdataInformationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author FNI18300
 */
@Component
public class SubtypeMapperConfigurer {

	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	public void addSubTypes() {
		objectMapper.registerSubtypes(new NamedType(SelfdataInformationRequest.class,
				SelfdataInformationRequest.class.getSimpleName()));
	}

}
