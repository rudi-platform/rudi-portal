package org.rudi.microservice.kalim.service.datafactory;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.MediaFile;

public class MediaFileDataFactory {

	private final ConnectorDataFactory connectorDataFactory = new ConnectorDataFactory();

	public MediaFile createMediaFile(UUID mediaId, String interfaceContractCode) {
		final MediaFile mediaFile = new MediaFile();
		mediaFile.setMediaId(mediaId);
		mediaFile.setConnector(connectorDataFactory.createConnector(interfaceContractCode));
		return mediaFile;
	}

}
