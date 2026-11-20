package org.rudi.microservice.selfdata.service.datafactory.matching;

import java.util.UUID;

import org.rudi.microservice.selfdata.core.bean.NodeProviderInfo;
import org.springframework.stereotype.Component;

@Component
public class NodeProviderInfoDataFactory {

	public NodeProviderInfo create(String url, UUID uuid) {
		return new NodeProviderInfo().url(url).uuid(uuid);
	}

}
