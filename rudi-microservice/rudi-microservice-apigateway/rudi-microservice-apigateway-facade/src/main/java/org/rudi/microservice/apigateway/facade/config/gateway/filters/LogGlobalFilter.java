/**
 * 
 */
package org.rudi.microservice.apigateway.facade.config.gateway.filters;

import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * https://stackoverflow.com/questions/74451046/spring-cloud-gateway-how-to-programatically-get-the-matched-route-information
 * 
 * 
 * @author FNI18300
 *
 */
@Slf4j
public class LogGlobalFilter extends AbstractGlobalFilter {

	public LogGlobalFilter(DatasetService datasetService) {
		super(datasetService);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.info("Call to " + formatMessage(exchange) + "...");
		return chain.filter(exchange).then(Mono.fromRunnable(() -> log
				.info("Call done to " + formatMessage(exchange) + "=>" + exchange.getResponse().getStatusCode())));
	}

	@Override
	public int getOrder() {
		return -1;
	}
}
