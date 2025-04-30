package org.rudi.microservice.apigateway.facade.config.gateway.exception;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-2)
public class GenericErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

	private static final List<Integer> HTTP_STATUS_3XX = List.of(HttpStatus.MOVED_PERMANENTLY.ordinal(),
			HttpStatus.FOUND.ordinal(), HttpStatus.TEMPORARY_REDIRECT.ordinal(),
			HttpStatus.PERMANENT_REDIRECT.ordinal());

	public GenericErrorWebExceptionHandler(ErrorAttributes errorAttributes, Resources resources,
			ErrorProperties errorProperties, ApplicationContext applicationContext) {
		super(errorAttributes, resources, errorProperties, applicationContext);
	}

	@Override
	protected Mono<ServerResponse> renderErrorView(ServerRequest request) {

		// To represent Error Attribute as a Map.
		Map<String, Object> errorPropertiesMap1 = getErrorAttributes(request, ErrorAttributeOptions.defaults());
		Map<String, Object> errorPropertiesMap2 = getErrorAttributesByMedia(request, MediaType.ALL);
		// To fetch response status code.
		HttpStatus status = HttpStatus.valueOf((Integer) errorPropertiesMap1.get("status"));

		if (status.is3xxRedirection() && HTTP_STATUS_3XX.contains(status.value())) {
			log.info("Redirection error: {} / {}", errorPropertiesMap1, errorPropertiesMap2);
			return super.renderErrorView(request);
		} else {
			return super.renderErrorView(request);
		}

	}

	private Map<String, Object> getErrorAttributesByMedia(ServerRequest request, MediaType mediaType) {
		return getErrorAttributes(request, getErrorAttributeOptions(request, mediaType));
	}

}
