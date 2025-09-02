package org.rudi.microservice.selfdata.service.helper.apigateway;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.microservice.apigateway.core.bean.Api;
import org.rudi.microservice.selfdata.service.exception.MissingApiForMediaException;
import org.rudi.microservice.selfdata.service.utils.MonoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SelfdataApiGatewayHelper {

	@Autowired
	@Qualifier("rudi_selfdata")
	private WebClient apigatewayWebClient;

	private final ApiGatewayWebClientProperties apigatewayProperties;

	public ClientResponse datasets(Api api, String token, MultiValueMap<String, String> queryParams)
			throws AppServiceNotFoundException {
		String apiHttpMethod = CollectionUtils.isNotEmpty(api.getMethods()) ? api.getMethods().get(0).getValue() : null;
		HttpMethod httpMethod = HttpMethod.valueOf(apiHttpMethod); // un HttpMethod est toujours retourné, même si la méthode n'est pas une méthode connue
		if (!ArrayUtils.contains(HttpMethod.values(), httpMethod)) {
			throw new IllegalArgumentException(
					String.format("Valid api method is required : no valid http method found for %s", apiHttpMethod));
		}
		var mono = apigatewayWebClient.method(httpMethod)
				.uri(uriBuilder -> uriBuilder.path(apigatewayProperties.getDatasetApiPath())
						.queryParam("globalId", api.getGlobalId()).queryParam("mediaId", api.getMediaId())
						.queryParam("contract", api.getContract()).queryParams(queryParams).build())
				.headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, token)).retrieve()
				.bodyToMono(ClientResponse.class);

		return MonoUtils.blockOrThrow(mono, e -> new MissingApiForMediaException(api.getGlobalId(), api.getMediaId()));
	}
}
