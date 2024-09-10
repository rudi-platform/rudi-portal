package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;

public abstract class AbstractConnectorParametersValidator
		implements ElementValidator<ConnectorConnectorParametersInner> {

	/**
	 * Valide que la valeur saisie est bien de type Integer
	 * 
	 * @param field Nom du champ
	 * @param value Valeur du champ
	 * @return Erreurs potentielles sinon set vide
	 */
	protected Set<IntegrationRequestErrorEntity> validateIntegerFormat(String field, String value) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			integrationRequestsErrors.add(buildError307(field, value));
		}
		return integrationRequestsErrors;
	}

	/**
	 * Construit une erreur de type 307
	 * 
	 * @param field champ concerné
	 * @param value valeur du champ ayant causée l'erreur
	 * @return l'erreur
	 */
	protected IntegrationRequestErrorEntity buildError307(String field, String value) {
		final var errorMessage = String.format(IntegrationError.ERR_307.getMessage(), value, field);
		return new IntegrationRequestErrorEntity(UUID.randomUUID(), IntegrationError.ERR_307.getCode(), errorMessage,
				field, LocalDateTime.now());
	}

	/**
	 * Construit une erreur de type 307
	 * 
	 * @param field champ concerné
	 * @param value valeur du champ ayant causée l'erreur
	 * @return l'erreur
	 */
	protected IntegrationRequestErrorEntity buildError307(String message, String field, String value) {
		final var errorMessage = String.format(message, value, field);
		return new IntegrationRequestErrorEntity(UUID.randomUUID(), IntegrationError.ERR_307.getCode(), errorMessage,
				field, LocalDateTime.now());
	}

	/**
	 * Construit une erreur de type 201
	 * 
	 * @param field champ concerné
	 * @param value valeur du champ ayant causée l'erreur
	 * @return l'erreur
	 */
	protected IntegrationRequestErrorEntity buildError201(String field, String value) {
		final var errorMessage = String.format(IntegrationError.ERR_201.getMessage(), field, "Integer", value);
		return new IntegrationRequestErrorEntity(UUID.randomUUID(), IntegrationError.ERR_201.getCode(), errorMessage,
				field, LocalDateTime.now());
	}

	/**
	 * Construit une erreur de type 302
	 * 
	 * @param field    champ concerné
	 * @param value    valeur du champ ayant causée l'erreur
	 * @param expected référentiel attendu
	 * @return l'erreur
	 */
	protected IntegrationRequestErrorEntity buildError302(String field, String value, String expected) {
		final var errorMessage = String.format(IntegrationError.ERR_302.getMessage(), field, value, expected);
		return new IntegrationRequestErrorEntity(UUID.randomUUID(), IntegrationError.ERR_302.getCode(), errorMessage,
				field, LocalDateTime.now());
	}
}
