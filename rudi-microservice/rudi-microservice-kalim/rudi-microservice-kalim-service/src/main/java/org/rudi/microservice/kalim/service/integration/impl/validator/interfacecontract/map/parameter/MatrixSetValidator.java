package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter;

import static org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter.MapConnectorParametersConstants.MATRIX_SET_PARAMETER;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

@Component
public class MatrixSetValidator extends AbstractMapConnectorParametersValidator {

	@Override
	public Set<IntegrationRequestErrorEntity> validate(ConnectorConnectorParametersInner connectorConnectorParametersInner, String interfaceContract) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();
		final String value = connectorConnectorParametersInner.getValue();
		if (StringUtils.isBlank(value)) {
			integrationRequestsErrors.add(buildError307(MATRIX_SET_PARAMETER, value));
		}
		return integrationRequestsErrors;
	}

	@Override
	public boolean accept(ConnectorConnectorParametersInner connectorConnectorParametersInner) {
		return connectorConnectorParametersInner.getKey().equals(MATRIX_SET_PARAMETER);
	}
}
