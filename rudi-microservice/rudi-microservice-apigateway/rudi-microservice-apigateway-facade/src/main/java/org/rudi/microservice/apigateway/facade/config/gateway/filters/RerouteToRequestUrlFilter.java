package org.rudi.microservice.apigateway.facade.config.gateway.filters;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.swagger.SwaggerHelper;
import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.microservice.apigateway.core.common.ParameterInformation;
import org.rudi.microservice.apigateway.facade.config.gateway.ApiGatewayConstants;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.InterfaceContratHelper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.PathContainer;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class RerouteToRequestUrlFilter extends AbstractGlobalFilter implements GlobalFilter, Ordered {

	private static final String INTERFACE_CONTRACT_PATH = "interface-contract";

	private static final String CONTRACT_URL_PARAMETER = "contract_url";

	private static final String CUSTOM_CONTRACT = "custom";

	public static final int ROUTE_TO_URL_FILTER_ORDER = 90000;

	private static final String SCHEME_REGEX = "[a-zA-Z]([a-zA-Z]|\\d|\\+|\\.|-)*:.*";

	private static final Pattern schemePattern = Pattern.compile(SCHEME_REGEX);

	private final InterfaceContratHelper interfaceContratHelper;

	private final SwaggerHelper swaggerHelper;

	public RerouteToRequestUrlFilter(DatasetService datasetService, SwaggerHelper swaggerHelper,
			InterfaceContratHelper interfaceContratHelper) {
		super(datasetService);
		this.interfaceContratHelper = interfaceContratHelper;
		this.swaggerHelper = swaggerHelper;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
		if (route == null) {
			return chain.filter(exchange);
		}
		log.trace("RerouteToRequestUrlFilter start");
		URI uri = exchange.getRequest().getURI();
		boolean encoded = ServerWebExchangeUtils.containsEncodedParts(uri);
		URI routeUri = route.getUri();

		if (hasAnotherScheme(routeUri)) {
			// this is a special url, save scheme to special attribute
			// replace routeUri with schemeSpecificPart
			exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR, routeUri.getScheme());
			routeUri = URI.create(routeUri.getSchemeSpecificPart());
		}

		if ("lb".equalsIgnoreCase(routeUri.getScheme()) && routeUri.getHost() == null) {
			// Load balanced URIs should always have a host. If the host is null it is
			// most
			// likely because the host name was invalid (for example included an
			// underscore)
			throw new IllegalStateException("Invalid host: " + routeUri.toString());
		}

		// on récupère les paramètres issus de l'url déclarée
		String oQuery = prepareRawQuery(routeUri);
		log.debug("RerouteToRequestUrlFilter with {} and {}", oQuery, uri.getQuery());

		// on récupère les paramètres issus de l'appel
		String iQuery = prepareQueryParam(exchange);

		checkParameters(exchange, uri);

		URI mergedUrl = UriComponentsBuilder.fromUri(uri).scheme(routeUri.getScheme()).host(routeUri.getHost())
				.port(routeUri.getPort()).path(routeUri.getPath()).query(StringUtils.join(List.of(oQuery, iQuery), '&'))
				.build(encoded).toUri();
		exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, mergedUrl);
		return chain.filter(exchange);
	}

	protected String prepareRawQuery(URI routeUri) {
		String oQuery = routeUri.getRawQuery();
		if (StringUtils.isNotEmpty(oQuery)) {
			try {
				return URLDecoder.decode(oQuery.replace("+", "%2B"), "UTF-8").replace("%2B", "+");
			} catch (Exception e) {
				log.warn("Failed to decode uri " + oQuery, e);
				return oQuery;
			}
		}
		return StringUtils.EMPTY;
	}

	protected String prepareQueryParam(ServerWebExchange exchange) {
		List<String> params = new ArrayList<>();
		MultiValueMap<String, String> parameters = exchange.getRequest().getQueryParams();
		for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();
			for (String value : values) {
				params.add(key + '=' + value);
			}
		}
		return StringUtils.join(params, '&');
	}

	protected void checkParameters(ServerWebExchange exchange, URI uri) {
		String contract = extractContract(exchange);

		if (CUSTOM_CONTRACT.equalsIgnoreCase(contract)) {
			try {
				Metadata metadata = getMetadata(UUID.fromString(extractGlobalId(exchange)));
				Media media = metadata.getAvailableFormats().stream()
						.filter(m -> m.getMediaId().equals(UUID.fromString(extractMediaId(exchange)))).findFirst()
						.orElse(null);
				if (media != null) {
					ConnectorConnectorParametersInner contractUrlParameter = media.getConnector()
							.getConnectorParameters().stream()
							.filter(p -> p.getKey().equalsIgnoreCase(CONTRACT_URL_PARAMETER)).findFirst().orElse(null);
					if (contractUrlParameter != null) {
						OpenAPI swagger = swaggerHelper.getSwaggerContractFromURL(contractUrlParameter.getValue());
						checkParameters(exchange, swagger, uri);
					}
				}
			} catch (Exception e) {
				log.warn("Failed to get contract", e);
			}
		} else {
			try {
				String contractPath = StringUtils.join(INTERFACE_CONTRACT_PATH, File.separator, contract, ".json");
				OpenAPI swagger = swaggerHelper.getSwaggerContractFromValue(contractPath);
				checkParameters(exchange, swagger, uri);
			} catch (Exception e) {
				log.warn("Failed to get contract", e);
			}
		}
	}

	protected String extractContract(ServerWebExchange exchange) {
		return extractPathPart(exchange, ApiGatewayConstants.CONTRACT_ID_INDEX);
	}

	protected String extractGlobalId(ServerWebExchange exchange) {
		return extractPathPart(exchange, ApiGatewayConstants.GLOBAL_ID_INDEX);
	}

	protected String extractMediaId(ServerWebExchange exchange) {
		return extractPathPart(exchange, ApiGatewayConstants.MEDIA_ID_INDEX);
	}

	protected String extractPathPart(ServerWebExchange exchange, int index) {
		PathContainer pathContainer = exchange
				.getAttribute(ServerWebExchangeUtils.GATEWAY_PREDICATE_PATH_CONTAINER_ATTR);
		if (pathContainer != null) {
			return pathContainer.subPath(index, index + 1).value();
		} else {
			return null;
		}
	}

	private void checkParameters(ServerWebExchange exchange, OpenAPI swagger, URI uri) {
		List<ParameterInformation> parameterInformations = interfaceContratHelper.checkParameters(swagger,
				exchange.getRequest().getMethod().name(), uri.getRawQuery());
		for (ParameterInformation parameterInformation : parameterInformations) {
			log.info("checkParameter {}", parameterInformation);
		}
	}

	@Override
	public int getOrder() {
		return ROUTE_TO_URL_FILTER_ORDER;
	}

	static boolean hasAnotherScheme(URI uri) {
		return schemePattern.matcher(uri.getSchemeSpecificPart()).matches() && uri.getHost() == null
				&& uri.getRawPath() == null;
	}

}
