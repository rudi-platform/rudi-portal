package org.rudi.microservice.strukture.service.datafactory.dto;

import java.util.UUID;

import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.springframework.stereotype.Component;

@Component
public class NodeProviderDtoDataFactory {

	public NodeProvider create(UUID uuid) {
		NodeProvider nodeProvider = new NodeProvider();
		nodeProvider.setUuid(uuid);
		return nodeProvider;
	}

}
