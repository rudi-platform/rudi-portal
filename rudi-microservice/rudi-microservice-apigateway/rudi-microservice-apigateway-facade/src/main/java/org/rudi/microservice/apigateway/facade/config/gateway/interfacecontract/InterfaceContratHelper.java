/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.microservice.apigateway.core.common.ParameterInformation;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter.ArrayParameterDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter.BooleanParameterDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter.IntegerParameterDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter.NumberParameterDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter.ParameterDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter.StringParameterDataValidator;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterfaceContratHelper {

	private static final Map<SwaggerType, ParameterDataValidator> PARAMETER_DATA_VALIDATORS = new EnumMap<>(
			SwaggerType.class);

	public List<ParameterInformation> checkParameters(OpenAPI openapi, String method, String queryParameters) {
		List<ParameterInformation> result = new ArrayList<>();
		Map<String, String> parameters = parseQueryParameters(queryParameters);
		PathItem path = openapi.getPaths().values().stream().findFirst().orElse(null);
		if (path != null) {
			Operation operation = path.getGet();
			if (operation == null) {
				operation = path.getPost();
			}
			if (operation != null) {
				checkParameters(openapi, result, operation.getParameters(), parameters);
			} else {
				log.warn("contract without requested operation {}", method);
			}
		} else {
			log.warn("contract with no path {}", openapi.getInfo());
		}
		return result;
	}

	private void checkParameters(OpenAPI openapi, List<ParameterInformation> parameterInformations,
			List<Parameter> swaggerParameters, Map<String, String> inputParameters) {
		for (Map.Entry<String, String> entry : inputParameters.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			Parameter parameter = lookupParameter(swaggerParameters, key);
			if (parameter == null) {
				parameterInformations.add(ParameterInformation.builder().parameterName(key).valid(false)
						.message("Parameter does not exists in contract").build());
			} else if ("query".equalsIgnoreCase(parameter.getIn())) {
				parameterInformations.add(ParameterInformation.builder().parameterName(key).valid(false)
						.message("Parameter exists in contract with in=" + parameter.getIn()).build());
			} else if (!isValidValue(openapi, parameter, val)) {
				parameterInformations.add(ParameterInformation.builder().parameterName(key).valid(false)
						.message("Invalid value for parameter=" + val).build());
			} else {
				parameterInformations.add(ParameterInformation.builder().parameterName(key).valid(true).build());
			}
		}
	}

	private boolean isValidValue(OpenAPI openapi, Parameter parameter, String value) {
		boolean valid = false;
		if (StringUtils.isEmpty(value)) {
			// les valeurs vides ou nulles sont considérée comme inoffensive
			valid = true;
		} else if (parameter instanceof QueryParameter) {
			// c'est un paramètre query ou form<br>
			// on exclut :
			// <ul>
			// <li>les PathParameter car on ne peut pas utiliser un morceau de path inconnu</li>
			// <li>les CookieParameter et HeaderParameter car on ne sait pas quoi controler</li>
			// </ul>
			//
			QueryParameter queryParameter = (QueryParameter) parameter;
			String openapiType = queryParameter.getSchema().getType();
			SwaggerType type = SwaggerType.lookupType(openapiType);
			ParameterDataValidator parameterDataValidator = PARAMETER_DATA_VALIDATORS.get(type);
			if (parameterDataValidator != null) {
				valid = parameterDataValidator.validate(openapi, queryParameter, value);
			} else {
				// cas non prévu on bloque pas
				valid = true;
			}
		} else {
			// cas non prévu on bloque pas
			valid = true;
		}
		return valid;
	}

	private Parameter lookupParameter(List<Parameter> parameters, String parameterName) {
		Parameter result = null;
		if (CollectionUtils.isNotEmpty(parameters)) {
			result = parameters.stream().filter(p -> p.getName().equals(parameterName)).findFirst().orElse(null);
		}
		return result;
	}

	public Map<String, String> parseQueryParameters(String rawQueryParameters) {
		Map<String, String> result = new HashMap<>();
		if (StringUtils.isNotEmpty(rawQueryParameters)) {
			String[] queryParameters = rawQueryParameters.split("&");
			for (String queryParameter : queryParameters) {
				String[] parameterValues = queryParameter.split("=");
				if (parameterValues.length == 1) {
					result.put(parameterValues[0], null);
				} else {
					result.put(parameterValues[0],
							StringUtils.join(ArrayUtils.subarray(parameterValues, 1, parameterValues.length)));
				}
			}
		}
		return result;
	}

	static {
		PARAMETER_DATA_VALIDATORS.put(SwaggerType.STRING, new StringParameterDataValidator());
		PARAMETER_DATA_VALIDATORS.put(SwaggerType.BOOLEAN, new BooleanParameterDataValidator());
		PARAMETER_DATA_VALIDATORS.put(SwaggerType.NUMBER, new NumberParameterDataValidator());
		PARAMETER_DATA_VALIDATORS.put(SwaggerType.INTEGER, new IntegerParameterDataValidator());
		PARAMETER_DATA_VALIDATORS.put(SwaggerType.ARRAY, new ArrayParameterDataValidator());
	}
}
