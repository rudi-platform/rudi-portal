package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.custom;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomConnectorParametersConstants {
	public static final String CONTRACT_URL_PARAMETER = "contract_url";
	public static final List<String> CUSTOM_MANDATORY_PARAMS = List.of(CONTRACT_URL_PARAMETER);

}
