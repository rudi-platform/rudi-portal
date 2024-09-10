package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter;

import static org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter.MapConnectorParametersConstants.VERSION_PARAMETER;

import java.util.HashSet;
import java.util.Set;

import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

@Component
public class VersionsValidator extends AbstractMapConnectorParametersValidator {

	@Override
	public Set<IntegrationRequestErrorEntity> validate(ConnectorConnectorParametersInner connectorConnectorParametersInner, String interfaceContract) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();

		final String value = connectorConnectorParametersInner.getValue();
		String[] splitValue = value.split("\\.");

		if (splitValue.length != 3) { // Format attendu : X.Y.Z
			integrationRequestsErrors.add(buildError307(VERSION_PARAMETER, value));
			return integrationRequestsErrors;
		}

		boolean parseError = false;
		int i = 0;
		while(!parseError && i<splitValue.length)  {
			try {
				Integer.parseInt(splitValue[i]);
				i += 1;
			} catch (NumberFormatException nfe) {
				integrationRequestsErrors.add(buildError307(VERSION_PARAMETER, value));
				parseError = true;
			}
		}

		return integrationRequestsErrors;
	}


	@Override
	public boolean accept(ConnectorConnectorParametersInner connectorConnectorParametersInner) {
		return connectorConnectorParametersInner.getKey().equals(VERSION_PARAMETER);
	}
}
