package org.rudi.microservice.strukture.service.config;

import java.util.Map;
import org.rudi.common.storage.dao.StampedRepositoryImpl;
import org.rudi.microservice.strukture.core.common.SchemaConstants;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = { "org.rudi.common.storage.entity", "org.rudi.facet.bpmn.entity",
		"org.rudi.microservice.strukture.storage.entity", "org.rudi.facet.doks.entity", })
@EnableJpaRepositories(basePackages = {"org.rudi.common.storage.dao", "org.rudi.facet.bpmn.dao",
		"org.rudi.microservice.strukture.storage.dao", "org.rudi.facet.doks.dao",}, repositoryBaseClass = StampedRepositoryImpl.class)
public class StruktureDatabaseConfiguration implements HibernatePropertiesCustomizer {

	@Override
	public void customize(Map<String, Object> hibernateProperties) {
		hibernateProperties.put("hibernate.default_schema", SchemaConstants.DATA_SCHEMA);
	}

}
