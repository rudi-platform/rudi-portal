package org.rudi.microservice.konsult.service.mapper.jsonld;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractJsonLdMapper<T>  implements JsonLdMapper<T> {

	@Value("${front.urlServer}")
	private String urlServer;

	private final MediaUrlHelper mediaUrlHelper;

	protected AbstractJsonLdMapper(MediaUrlHelper mediaUrlHelper) {
		this.mediaUrlHelper = mediaUrlHelper;
	}

	protected String getBaseUrl(){
		return StringUtils.removeEnd(urlServer, "/");
	}

	protected String rewriteMediaUrl(Media media, Metadata metadata) {
		mediaUrlHelper.rewriteMediaUrl(metadata, media);

		return getBaseUrl().concat(media.getConnector().getUrl());
	}

	protected JsonObject getMediaAsJsonObject(Media media, String type, DcatJsonLdContext context) {
		JsonObject rs = new JsonObject();

		rs.addProperty("@id", rewriteMediaUrl(media, context.getCurentMetadata()));
		rs.addProperty("@type", type);
		rs.addProperty("identifier", media.getMediaId().toString());

		if(io.micrometer.common.util.StringUtils.isNotEmpty(media.getMediaName())){
			rs.addProperty("title", media.getMediaName());
		}
		else {
			rs.addProperty("title", context.getCurentMetadata().getResourceTitle());
		}
		if(io.micrometer.common.util.StringUtils.isNotEmpty(media.getMediaCaption())){
			rs.addProperty("description", media.getMediaCaption());
		}

		if(media.getMediaDates() != null){
			rs.addProperty("issued", media.getMediaDates().getCreated().toString());
			rs.addProperty("modified", media.getMediaDates().getUpdated().toString());
		}

		if(context.getLicense() != null){
			rs.add("license", context.getLicense());
		}


		return rs;
	}

}
