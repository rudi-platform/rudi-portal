package org.rudi.tools.nodestub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Operation(operationId = "test", responses = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.springframework.core.io.Resource.class))) })
	@GetMapping(value = "/nodestub/test", produces = { "application/octet-stream", "application/json" })
	ResponseEntity<String> getTestqp(@Parameter(name = "parameter1", in = ParameterIn.QUERY) String parameter1) {
		return ResponseEntity.ok(parameter1);
	}

	@Operation(operationId = "test", responses = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.springframework.core.io.Resource.class))) })
	@PostMapping(value = "/nodestub/test", produces = { "application/octet-stream", "application/json" })
	ResponseEntity<String> postTestqp(@Parameter(name = "parameter1", in = ParameterIn.QUERY) String parameter1,
			@Parameter(name = "parameter2") @Valid @RequestBody(required = false) String parameter2) {
		return ResponseEntity.ok(parameter1 + "-" + parameter2);
	}
}