package org.rudi.microservice.konsult.facade.exception;

import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.microservice.konsult.core.bean.ApiError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class KonsultExceptionHandler {

	@ExceptionHandler(DataverseAPIException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	protected @ResponseBody ApiError handleDataverseService(final DataverseAPIException ex, final WebRequest request) {

		log.error(ex.getMessage(), ex);

		ApiError apiError = new ApiError();
		if (ex.getApiResponseInfo() != null) {
			apiError.setCode(ex.getApiResponseInfo().getStatus());
		} else {
			apiError.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		}
		apiError.setLabel(ex.getMessage());

		return apiError;
	}

}
