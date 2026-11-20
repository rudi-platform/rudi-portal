package org.rudi.microservice.kalim.service.datafactory;

import java.util.UUID;

import org.rudi.microservice.apigateway.core.bean.Api;

public class ApiDataFactory {

	public Api createApi() {
		return new Api();
	}

	public Api createApiWithId(UUID apiId) {
		return new Api().apiId(apiId);
	}

	public Api createApi(UUID apiId, UUID globalId, UUID mediaId) {
		return new Api().apiId(apiId).globalId(globalId).mediaId(mediaId);
	}

}
