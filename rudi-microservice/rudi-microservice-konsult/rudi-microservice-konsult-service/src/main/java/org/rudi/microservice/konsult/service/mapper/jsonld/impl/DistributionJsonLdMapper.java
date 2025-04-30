package org.rudi.microservice.konsult.service.mapper.jsonld.impl;

import com.google.gson.JsonObject;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.AbstractJsonLdMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DistributionJsonLdMapper extends AbstractJsonLdMapper<MediaFile> {


	public DistributionJsonLdMapper(MediaUrlHelper mediaUrlHelper) {
		super(mediaUrlHelper);
	}

	@Override
	public JsonObject toJsonLd(MediaFile media, DcatJsonLdContext context) throws AppServiceException {
		JsonObject result = getMediaAsJsonObject(media, "Distribution", context);

		result.addProperty("downloadURL", rewriteMediaUrl(media, context.getCurentMetadata()));
		result.addProperty("mediaType", media.getFileType().getValue());
		result.addProperty("byteSize", media.getFileSize());
		result.addProperty("format", media.getConnector().getInterfaceContract());

		result.add("license", context.getLicense()); // @ID = url sur la licence...



		return result;
	}


}
