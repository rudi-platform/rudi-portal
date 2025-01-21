package org.rudi.facet.dataverse.api;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.dataverse.model.DataverseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class AbstractDataverseAPI {

	private final ObjectMapper objectMapper;

	@Autowired
	@Qualifier("rudi_dataverse_client")
	@Getter
	private WebClient webClient;

	protected String createUrl(String... pathComponents) {
		return "/" + StringUtils.join(pathComponents, "/");
	}

	protected String marshalObject(Object object) throws DataverseAPIException {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new DataverseAPIException(e);
		}
	}

	protected Mono<? extends Throwable> handleError(String message, Throwable error) {
		if (error instanceof WebClientResponseException && error.getCause() != null) {
			error = error.getCause();
		}
		return Mono.error(new DataverseAPIException(error.getMessage(), error));
	}

	protected <T extends Serializable> T getDataBody(DataverseResponse<T> dataverseResponse) {
		return dataverseResponse != null ? dataverseResponse.getData() : null;
	}

}
