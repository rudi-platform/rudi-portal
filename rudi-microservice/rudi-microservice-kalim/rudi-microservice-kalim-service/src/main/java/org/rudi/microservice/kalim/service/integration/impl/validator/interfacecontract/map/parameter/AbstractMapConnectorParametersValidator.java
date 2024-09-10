package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter;

import static org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter.MapConnectorParametersConstants.EPSG_TEXT;

import java.util.HashSet;
import java.util.Set;

import org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.AbstractConnectorParametersValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;

public abstract class AbstractMapConnectorParametersValidator extends AbstractConnectorParametersValidator {
	/**
	 * Valide qu'on a un format EPSG
	 * 
	 * @param field Nom du champ
	 * @param value Valeur du champ
	 * @return Erreurs potentielles sinon set vide
	 */
	protected Set<IntegrationRequestErrorEntity> validateFormatEPSG(String field, String value) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();

		String[] splitValue = value.split(":");
		// Format attendu EPSG:XYZ
		if (splitValue.length != 2 || !splitValue[0].equalsIgnoreCase(EPSG_TEXT)) {
			integrationRequestsErrors.add(buildError307(field, value));
			return integrationRequestsErrors;
		}

		try {
			Integer.parseInt(splitValue[1]);
		} catch (NumberFormatException nfe) {
			integrationRequestsErrors.add(buildError307(field, value));
		}
		return integrationRequestsErrors;
	}

}
