package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.Connector;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer des objets Connector.
 * Un Connector représente l'accès à un média (URL, contrat d'interface, etc.).
 */
@Component
@RequiredArgsConstructor
public class ConnectorDataFactory {

	/**
	 * Crée un Connector standard avec URL et contrat d'interface.
	 */
	public Connector createConnectorStandard() {
		Connector connector = new Connector();
		connector.setUrl("www.connector1.org");
		connector.setInterfaceContract("interface contrat 1");
		return connector;
	}

	/**
	 * Crée un Connector pour un flux de série de données.
	 */
	public Connector createConnectorSeries() {
		Connector connector = new Connector();
		connector.setUrl("www.connector2.org");
		connector.setInterfaceContract("interface contrat 2");
		return connector;
	}

}
