package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter;

import static org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter.MapConnectorParametersConstants.MAX_ZOOM_PARAMETER;

import java.util.Set;

import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

@Component
public class MaxZoomValidator extends AbstractMapConnectorParametersValidator {

	@Override
	public Set<IntegrationRequestErrorEntity> validate(ConnectorConnectorParametersInner connectorConnectorParametersInner, String interfaceContract) {
		return validateIntegerFormat(MAX_ZOOM_PARAMETER, connectorConnectorParametersInner.getValue());
	}

	@Override
	public boolean accept(ConnectorConnectorParametersInner connectorConnectorParametersInner) {
		return connectorConnectorParametersInner.getKey().equals(MAX_ZOOM_PARAMETER);
	}
}
