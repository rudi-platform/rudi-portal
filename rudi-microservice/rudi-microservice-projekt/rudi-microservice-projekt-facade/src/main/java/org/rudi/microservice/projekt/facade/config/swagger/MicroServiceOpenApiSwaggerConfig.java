package org.rudi.microservice.projekt.facade.config.swagger;

import org.rudi.common.facade.config.swagger.OpenApiSwaggerConfig;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicroServiceOpenApiSwaggerConfig extends OpenApiSwaggerConfig {

	@Bean
	public GroupedOpenApi publicMgn() {
		return GroupedOpenApi.builder().group("projekt").packagesToScan("org.rudi.microservice.projekt.facade.controller").build();
	}

}
