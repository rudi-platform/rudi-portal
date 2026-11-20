package org.rudi.microservice.kalim.service.datafactory;

import org.rudi.facet.kaccess.bean.Connector;

public class ConnectorDataFactory {

	public Connector createConnector(String interfaceContractCode) {
		final Connector connector = new Connector();
		connector.setUrl("https://example.org/api");
		connector.setInterfaceContract(interfaceContractCode);
		return connector;
	}

}
