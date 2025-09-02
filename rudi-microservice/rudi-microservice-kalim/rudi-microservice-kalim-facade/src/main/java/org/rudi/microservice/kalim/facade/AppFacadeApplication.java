package org.rudi.microservice.kalim.facade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe de configuration globale de l'application.
 */
@SpringBootApplication(scanBasePackages = {
		"org.rudi.common.core",
		"org.rudi.common.facade",
		"org.rudi.common.service",
		"org.rudi.common.storage",
		"org.rudi.facet.acl",
		"org.rudi.facet.apigateway",
		"org.rudi.facet.dataverse",
		"org.rudi.facet.email",
		"org.rudi.facet.generator",
		"org.rudi.facet.generator.text",
		"org.rudi.facet.kaccess",
		"org.rudi.facet.kos",
		"org.rudi.facet.organization",
		"org.rudi.facet.projekt",
		"org.rudi.facet.providers",
		"org.rudi.facet.strukture",
		"org.rudi.microservice.kalim.facade",
		"org.rudi.microservice.kalim.service",
		"org.rudi.microservice.kalim.storage",
})
@EnableScheduling
@EnableDiscoveryClient(autoRegister = true)
@PropertySource(value = { "classpath:kalim/kalim-common.properties" })
@PropertySource(value = { "classpath:kalim/kalim-email.properties" })
public class AppFacadeApplication extends SpringBootServletInitializer {

	public static void main(final String[] args) {

		// Renomage du fichier de properties pour éviter les conflits avec d'autres
		// applications sur le tomcat
		System.setProperty("spring.config.name", "kalim");
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(AppFacadeApplication.class, args);

	}

	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
		return application.sources(AppFacadeApplication.class);
	}

}
