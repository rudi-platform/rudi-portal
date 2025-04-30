package org.rudi.microservice.konsult.service.helper.media;

import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.Metadata;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MediaUrlHelper {

	public void rewriteMediaUrls(Metadata metadata) {
		for (final Media media : metadata.getAvailableFormats()) {
			this.rewriteMediaUrl(metadata, media);
		}
	}

	/**
	 * On crée l'url tel qu'attendu à partir des metadata et du media :
	 *
	 * /media/{globalId}/{mediaId}/{contact} ou /media/4ff87569-dafc-45ad-ae5b-fac9a5ccbbb1/5d3ef922-bc72-4a89-84ef-21138b512f78/dwnl
	 *
	 */
	public void rewriteMediaUrl(Metadata metadata, Media media) {
		String url = String.format("/medias/%s/%s/%s", metadata.getGlobalId().toString(), media.getMediaId().toString(),
				media.getConnector().getInterfaceContract());
		media.getConnector().setUrl(url);
	}
}
