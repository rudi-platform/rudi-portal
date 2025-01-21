package org.rudi.common.facade.config.swagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

public class OpenApiSwaggerConfig {

	@Value("${swagger-server:}")
	private List<String> serverUrls;

	@Bean
	public GroupedOpenApi commonApi() {
		return GroupedOpenApi.builder().group("common-api").packagesToScan("org.rudi.common.facade.controller.api")
				.build();
	}

	@Bean
	public OpenAPI springOpenAPI() {
		OpenAPI openApi = new OpenAPI().openapi("3.0.0").info(new Info().title("API Search"))
				.components(new Components().addSecuritySchemes("jwt-oauth2",
						new SecurityScheme().type(Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.security(Collections.singletonList(new SecurityRequirement().addList("jwt-oauth2")));

		if (CollectionUtils.isNotEmpty(serverUrls)) {
			openApi.servers(computeServers());
		}
		return openApi;
	}

	protected List<Server> computeServers() {
		List<Server> result = new ArrayList<>();
		for (String serverUrl : serverUrls) {
			Server server = new Server();
			server.setUrl(serverUrl);
			result.add(server);
		}
		return result;
	}

}
