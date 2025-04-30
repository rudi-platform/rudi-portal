package org.rudi.microservice.konsult.service.helper.jsonld;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContextHelper {

	private static final String JSONLD_PATH = "harvest/jsonld-context.json";
	private final JsonResourceReader jsonResourceReader;


	public JsonObject getContext() throws AppServiceException {
		JsonObject context;

		try {
			Map<String, Object> map = jsonResourceReader.readMap(JSONLD_PATH);

			Gson gson = new Gson();
			JsonElement jsonElement = gson.toJsonTree(map);
			context = jsonElement.getAsJsonObject();

		}catch (IOException e){
			throw new AppServiceException("Erreur lors de la récupération du context", e);
		}

		return context;
	}
}
