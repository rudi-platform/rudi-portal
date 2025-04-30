package org.rudi.microservice.konsult.service.mapper.jsonld;

import java.util.UUID;

import com.google.gson.JsonObject;
import io.micrometer.common.util.StringUtils;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public abstract class AbstractKindJsonLdMapper<T> extends AbstractJsonLdMapper<T>{

	protected AbstractKindJsonLdMapper(MediaUrlHelper mediaUrlHelper) {
		super(mediaUrlHelper);
	}

	public JsonObject toJsonLd(UUID id, String value, String email, String url) {
		JsonObject kind = new JsonObject();
		kind.addProperty("@id",id.toString());
		kind.addProperty("@type","vcard:Kind");
		kind.addProperty("fn",value);

		if(StringUtils.isNotEmpty(email)){
			JsonObject hasEmail = new JsonObject();
			hasEmail.addProperty("@id",String.format("mailto:%s",email));

			kind.add("vcard:hasEmail",hasEmail);
		}

		if(StringUtils.isNotEmpty(url)){
			JsonObject hasUrl = new JsonObject();
			hasUrl.addProperty("@id", url);

			kind.add("vcard:hasUrl",hasUrl);
		}

		return kind;
	}
}
