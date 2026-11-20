package org.rudi.microservice.kalim.service.datafactory;

import java.util.UUID;

import org.rudi.facet.providers.bean.NodeProvider;

public class NodeProviderDataFactory {

	public NodeProvider createNodeProvider(UUID uuid) {
		return new NodeProvider().uuid(uuid);
	}

}
