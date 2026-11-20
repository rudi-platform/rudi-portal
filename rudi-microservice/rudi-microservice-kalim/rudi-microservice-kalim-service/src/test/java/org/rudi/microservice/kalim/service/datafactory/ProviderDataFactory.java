package org.rudi.microservice.kalim.service.datafactory;

import java.util.UUID;

import org.rudi.facet.providers.bean.NodeProvider;
import org.rudi.facet.providers.bean.Provider;

public class ProviderDataFactory {

	public Provider createProvider(UUID uuid) {
		return new Provider().uuid(uuid);
	}

	public Provider createProviderWithNodeProvider(UUID uuid, NodeProvider nodeProvider) {
		return new Provider().uuid(uuid).addNodeProvidersItem(nodeProvider);
	}

}
