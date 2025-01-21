package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.custom;

import static org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.custom.CustomConnectorParametersConstants.CONTRACT_URL_PARAMETER;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.swagger.SwaggerHelper;
import org.rudi.facet.dataset.bean.InterfaceContract;
import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.AbstractConnectorParametersValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor
public class ContractUrlValidator extends AbstractConnectorParametersValidator {

	private static final String ONE_ENTRY_POINT_MESSAGE = "Le contrat d'interface custom (%s) doit désigner un contrat OpenAPI 3.0 contenant un seul point d'entrée (au lieu de %d)";

	private static final String ONE_PATH_MESSAGE = "Le contrat d'interface custom (%s) doit désigner un contrat OpenAPI 3.0 contenant un seul point d'entrée muni d'un seul path (au lieu de %d)";

	private static final String INVALID_VERBS_MESSAGE = "Le contrat d'interface custom (%s) doit désigner un contrat OpenAPI 3.0 contenant un seul point d'entrée muni d'un seul path pour les verbes POST et/ou GET (au lieu de %s)";

	private static final String INVALID_SWAGGER_MESSAGE = "Le contrat d'interface custom (%s) doit désigner un contrat OpenAPI 3.0 accessible par le portail (%s)";

	private final SwaggerHelper swaggerHelper;

	@Override
	public Set<IntegrationRequestErrorEntity> validate(
			ConnectorConnectorParametersInner connectorConnectorParametersInner, String interfaceContract) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();

		if (InterfaceContract.CUSTOM.getCode().equalsIgnoreCase(interfaceContract)) {

			final String value = connectorConnectorParametersInner.getValue();
			if (StringUtils.isBlank(value)) {
				validateContractUrl(integrationRequestsErrors);
			} else {

				try {
					OpenAPI swagger = swaggerHelper.getSwaggerContractFromURL(value);
					validateEntryPoint(swagger, integrationRequestsErrors);

					PathItem path = swagger.getPaths().values().stream().findFirst().orElse(null);
					valideOneOperation(path, integrationRequestsErrors);
					valideMethods(path, integrationRequestsErrors);
				} catch (IOException e) {
					integrationRequestsErrors
							.add(buildError307(INVALID_SWAGGER_MESSAGE, value, CONTRACT_URL_PARAMETER));
				}
			}
		}

		return integrationRequestsErrors;
	}

	private void valideMethods(PathItem path, Set<IntegrationRequestErrorEntity> integrationRequestsErrors) {
		if (integrationRequestsErrors.isEmpty()) {
			List<Operation> invalidMethods = new ArrayList<>();
			if (path.getPatch() != null) {
				invalidMethods.add(path.getPatch());
			}
			if (path.getPut() != null) {
				invalidMethods.add(path.getPut());
			}
			if (path.getDelete() != null) {
				invalidMethods.add(path.getDelete());
			}
			if (path.getOptions() != null) {
				invalidMethods.add(path.getOptions());
			}
			valideMethods(invalidMethods, integrationRequestsErrors);
		}
	}

	private void valideOneOperation(PathItem pathItem, Set<IntegrationRequestErrorEntity> integrationRequestsErrors) {
		if (pathItem == null || (pathItem.getGet() == null && pathItem.getPost() == null)
				|| (pathItem.getGet() != null && pathItem.getPost() != null)) {
			int count = (pathItem == null || (pathItem.getGet() == null && pathItem.getPost() == null)) ? 0 : 1;
			count += (pathItem != null && pathItem.getGet() != null && pathItem.getPost() != null) ? 1 : 0;
			integrationRequestsErrors
					.add(buildError307(ONE_PATH_MESSAGE, Integer.toString(count), CONTRACT_URL_PARAMETER));
		}
	}

	private void valideMethods(List<Operation> invalidMethods,
			Set<IntegrationRequestErrorEntity> integrationRequestsErrors) {
		if (CollectionUtils.isNotEmpty(invalidMethods)) {
			integrationRequestsErrors
					.add(buildError307(INVALID_VERBS_MESSAGE, invalidMethods.toString(), CONTRACT_URL_PARAMETER));
		}
	}

	private void validateEntryPoint(OpenAPI swagger, Set<IntegrationRequestErrorEntity> integrationRequestsErrors) {
		if (swagger.getPaths().isEmpty() || swagger.getPaths().size() > 1) {
			integrationRequestsErrors.add(buildError307(ONE_ENTRY_POINT_MESSAGE,
					String.valueOf(swagger.getPaths().size()), CONTRACT_URL_PARAMETER));
		}
	}

	private void validateContractUrl(Set<IntegrationRequestErrorEntity> integrationRequestsErrors) {
		final var errorMessage = String.format(IntegrationError.ERR_308.getMessage(), CONTRACT_URL_PARAMETER);
		val error = new IntegrationRequestErrorEntity(UUID.randomUUID(), IntegrationError.ERR_308.getCode(),
				errorMessage, CONTRACT_URL_PARAMETER, LocalDateTime.now());
		integrationRequestsErrors.add(error);
	}

	@Override
	public boolean accept(ConnectorConnectorParametersInner connectorConnectorParametersInner) {
		return connectorConnectorParametersInner.getKey().equals(CONTRACT_URL_PARAMETER);
	}
}
