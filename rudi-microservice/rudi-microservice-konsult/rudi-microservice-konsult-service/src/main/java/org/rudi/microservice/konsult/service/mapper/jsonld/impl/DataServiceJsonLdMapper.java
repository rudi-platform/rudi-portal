package org.rudi.microservice.konsult.service.mapper.jsonld.impl;

import com.google.gson.JsonObject;
import io.micrometer.common.util.StringUtils;
import org.rudi.facet.kaccess.bean.MediaService;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.AbstractJsonLdMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataServiceJsonLdMapper extends AbstractJsonLdMapper<MediaService> {

	public DataServiceJsonLdMapper(MediaUrlHelper mediaUrlHelper) {
		super(mediaUrlHelper);
	}

	@Override
	public JsonObject  toJsonLd(MediaService media, DcatJsonLdContext context) {
		JsonObject ds = getMediaAsJsonObject(media, "DataService", context);


		ds.add("contactPoint", context.getContactPoints());

		JsonObject endpointURL = new JsonObject();
		endpointURL.addProperty("@id", rewriteMediaUrl(media, context.getCurentMetadata()));
		ds.add("endpointURL", endpointURL);

		if(StringUtils.isNotEmpty(media.getApiDocumentationUrl())) {
			JsonObject endpointDescription = new JsonObject();
			endpointDescription.addProperty("@id", media.getApiDocumentationUrl());
			ds.add("endpointDescription", endpointDescription);
		}

		if(context.getKeywords() != null && !context.getKeywords().isEmpty()){
			ds.add("keyword", context.getKeywords());
		}

		return ds;
	}
}
