/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway;

import org.rudi.common.service.swagger.SwaggerHelper;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.kaccess.helper.dataset.metadatadetails.MetadataDetailsHelper;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.facet.selfdata.helper.SelfdataHelper;
import org.rudi.microservice.apigateway.facade.config.gateway.filters.AccessControlGlobalFilter;
import org.rudi.microservice.apigateway.facade.config.gateway.filters.DatasetDecryptGatewayFilterFactory;
import org.rudi.microservice.apigateway.facade.config.gateway.filters.LogGlobalFilter;
import org.rudi.microservice.apigateway.facade.config.gateway.filters.RerouteToRequestUrlFilter;
import org.rudi.microservice.apigateway.facade.config.gateway.filters.SelfdataGlobalFilter;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.InterfaceContratHelper;
import org.rudi.microservice.apigateway.service.api.ApiService;
import org.rudi.microservice.apigateway.service.encryption.EncryptionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

/**
 * @author FNI18300
 *
 */
@Configuration
@RequiredArgsConstructor
public class ApiGatewayConfiguration extends GatewayAutoConfiguration {

	private final ApiService apiService;

	private final EncryptionService encryptionService;

	@Bean
	@ConditionalOnMissingBean(RouteDefinitionRepository.class)
	@Override
	public InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository() {
		return new ApiPathRouteDefinitionLocator(apiService, encryptionService);
	}

	@Bean
	public GlobalFilter logFilters(DatasetService datasetService) {
		return new LogGlobalFilter(datasetService);
	}

	@Bean
	public GlobalFilter selfdataFilters(DatasetService datasetService, ProjektHelper projektHelper, ACLHelper aclHelper,
			SelfdataHelper selfdataHelper, MetadataDetailsHelper metadataDetailsHelper) {
		return new SelfdataGlobalFilter(datasetService, projektHelper, aclHelper, selfdataHelper,
				metadataDetailsHelper);
	}

	@Bean
	public GlobalFilter accessControlFilters(DatasetService datasetService, ProjektHelper projektHelper,
			ACLHelper aclHelper, SelfdataHelper selfdataHelper, MetadataDetailsHelper metadataDetailsHelper) {
		return new AccessControlGlobalFilter(datasetService, projektHelper, aclHelper, selfdataHelper,
				metadataDetailsHelper);
	}

	@Bean
	public RerouteToRequestUrlFilter rerouteToRequestUrlFilter(DatasetService datasetService,
			SwaggerHelper swaggerHelper, InterfaceContratHelper interfaceContratHelper) {
		return new RerouteToRequestUrlFilter(datasetService, swaggerHelper, interfaceContratHelper);
	}

	@Bean
	@ConditionalOnEnabledFilter
	public DatasetDecryptGatewayFilterFactory datasetDecryptGatewayFilterFactory(EncryptionService encryptionService,
			DatasetService datasetService) {
		return new DatasetDecryptGatewayFilterFactory(encryptionService, datasetService);
	}

}
