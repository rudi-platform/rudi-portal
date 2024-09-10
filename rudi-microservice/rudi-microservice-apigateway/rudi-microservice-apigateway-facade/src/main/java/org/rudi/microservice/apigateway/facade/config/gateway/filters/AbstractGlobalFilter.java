/**
 * 
 */
package org.rudi.microservice.apigateway.facade.config.gateway.filters;

import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.microservice.apigateway.facade.config.gateway.ApiGatewayConstants;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * https://stackoverflow.com/questions/74451046/spring-cloud-gateway-how-to-programatically-get-the-matched-route-information
 * 
 * 
 * @author FNI18300
 *
 */
@AllArgsConstructor
public abstract class AbstractGlobalFilter implements GlobalFilter, Ordered {

	@Getter(value = AccessLevel.PROTECTED)
	private final DatasetService datasetService;

	protected String formatMessage(ServerWebExchange exchange) {
		return exchange.getLogPrefix() + " " + exchange.getRequest().getMethodValue() + " "
				+ exchange.getRequest().getURI();
	}

	/**
	 * 
	 * @param exchange le webexchange en cours
	 * @return le login du user
	 */
	protected String getAuthenticatedUser(ServerWebExchange exchange) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() != null) {
			return authentication.getPrincipal().toString();
		} else {
			return ApiGatewayConstants.APIGATEWAY_UNKNOWN_LOGIN;
		}
	}

	protected Metadata getMetadata(UUID globalId) throws DataverseAPIException {
		return datasetService.getDataset(globalId);
	}

	protected Pair<UUID, UUID> extractDatasetIdentifiers(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();
		UUID globalId = UUID.fromString(request.getPath()
				.subPath(ApiGatewayConstants.GLOBAL_ID_INDEX, ApiGatewayConstants.GLOBAL_ID_INDEX + 1).toString());
		UUID mediaId = UUID.fromString(request.getPath()
				.subPath(ApiGatewayConstants.MEDIA_ID_INDEX, ApiGatewayConstants.MEDIA_ID_INDEX + 1).toString());
		return Pair.of(globalId, mediaId);
	}
}
