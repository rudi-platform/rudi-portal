package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.dataset.bean.InterfaceContract;
import org.rudi.facet.kaccess.bean.Connector;
import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.rudi.microservice.kalim.service.integration.impl.validator.Error307Builder;
import org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.AbstractConnectorParametersValidator;
import org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.InterfaceContractConnectorValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConnectorValidator {
	private final List<AbstractConnectorParametersValidator> connectorParameterValidators;
	private final List<InterfaceContractConnectorValidator> interfaceContactConnectorValidators;

	public Set<IntegrationRequestErrorEntity> validate(Connector connector) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();
		InterfaceContract interfaceContract = InterfaceContract.from("code", connector.getInterfaceContract(),
				(contract -> connector.getInterfaceContract().equalsIgnoreCase(contract.getCode())), true);

		if (interfaceContract == null) {
			// désormais le contrat doit être connu
			integrationRequestsErrors.add(new Error307Builder().fieldValue(connector.getInterfaceContract())
					.field(RudiMetadataField.MEDIA_CONNECTOR_INTERFACE_CONTRACT).build());
		} else if (interfaceContract.isValidable()) {
			// Permet de ne pas trester les connectors parameters si on est dans le cas d'un service non-géo - Exemple : DWNL

			checkMandatoryParameters(interfaceContract, connector.getConnectorParameters(), integrationRequestsErrors);
			if (!integrationRequestsErrors.isEmpty()) { // Si des champs obligatoires sont manquants, on arrête là
				return integrationRequestsErrors;
			}

			// Pas de champs obligatoires manquants, validation de ceux renseignés
			for (ConnectorConnectorParametersInner parameterInner : connector.getConnectorParameters()) {
				connectorParameterValidators.stream().filter(element -> element.accept(parameterInner)).findFirst()
						.ifPresent(validator -> integrationRequestsErrors
								.addAll(validator.validate(parameterInner, connector.getInterfaceContract())));
			}
		}
		return integrationRequestsErrors;
	}

	/**
	 * Vérifie pour chaque interface contract que tous ses champs obligatoires sont fournis dans le tableau des connectorParameters
	 *
	 * @param interfaceContract   wms/wmts/wfs
	 * @param connectorParameters tableau des paramètres fournis
	 * @param errorEntities       set d'erreur alimenté en cas de param obligatoire manquant
	 */
	private void checkMandatoryParameters(InterfaceContract interfaceContract,
			List<ConnectorConnectorParametersInner> connectorParameters,
			Set<IntegrationRequestErrorEntity> errorEntities) {
		List<String> connectorParametersKeys = CollectionUtils.emptyIfNull(connectorParameters).stream()
				.map(ConnectorConnectorParametersInner::getKey).collect(Collectors.toList());

		for (InterfaceContractConnectorValidator validator : interfaceContactConnectorValidators) {
			if (validator.accept(interfaceContract)) {
				validator.validate(connectorParametersKeys, errorEntities);
			}
		}
	}

}
