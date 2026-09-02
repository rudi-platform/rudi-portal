package org.rudi.facet.kaccess.datafactory;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.MediaFile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets MediaFile.
 * Un MediaFile représente un fichier de données.
 */
@Component
@RequiredArgsConstructor
public class MediaFileDataFactory {

	private static final UUID MEDIA_ID_PDF = UUID.fromString("5f0fb0d9-b9b3-4a03-b5c1-b42e63a2c3e7");
	private static final UUID MEDIA_ID_XML = UUID.fromString("7a94b85a-9c8b-4639-8894-fd68c4c94b18");
	private static final UUID MEDIA_ID_MINIMAL = UUID.fromString("6f72a88f-2872-44e4-987f-b8b154379271");

	private final ConnectorDataFactory connectorDataFactory;
	private final ChecksumDataFactory checksumDataFactory;

	/**
	 * Crée un fichier PDF standard.
	 * Le mediaId est fixe pour garantir la stabilité des fixtures de référence.
	 */
	public MediaFile createMediaFilePdf() {
		MediaFile mediaFile = new MediaFile();
		mediaFile.setMediaId(MEDIA_ID_PDF);
		mediaFile.setMediaType(MediaFile.MediaTypeEnum.FILE);
		mediaFile.setConnector(connectorDataFactory.createConnectorStandard());
		mediaFile.setFileType("application/pdf");
		mediaFile.setFileSize(250L);
		mediaFile.setFileStructure("https://filestructure.org/");
		mediaFile.setFileEncoding("ISO-8859-1");
		mediaFile.setChecksum(checksumDataFactory.createChecksumMd5());
		return mediaFile;
	}

	/**
	 * Crée un fichier XML standard.
	 * Le mediaId est fixe pour garantir la stabilité des fixtures de référence.
	 */
	public MediaFile createMediaFileXml() {
		MediaFile mediaFile = new MediaFile();
		mediaFile.setMediaId(MEDIA_ID_XML);
		mediaFile.setMediaType(MediaFile.MediaTypeEnum.FILE);
		mediaFile.setConnector(connectorDataFactory.createConnectorStandard());
		mediaFile.setFileType("text/xml");
		mediaFile.setFileSize(406L);
		mediaFile.setFileStructure("https://filestructure.org/");
		mediaFile.setFileEncoding("UTF-8");
		mediaFile.setChecksum(checksumDataFactory.createChecksumSha256());
		return mediaFile;
	}

	/**
	 * Crée un fichier CSV minimal.
	 * Le mediaId est fixe pour garantir la stabilité des fixtures de référence.
	 */
	public MediaFile createMediaFileMinimal() {
		MediaFile mediaFile = new MediaFile();
		mediaFile.setMediaId(MEDIA_ID_MINIMAL);
		mediaFile.setMediaType(MediaFile.MediaTypeEnum.FILE);
		mediaFile.setMediaCaption("On est obligé de mettre au moins un média pour respecter le swagger");
		mediaFile.setConnector(connectorDataFactory.createConnectorStandard());
		mediaFile.setFileType("text/csv");
		mediaFile.setFileSize(50L);
		mediaFile.setChecksum(checksumDataFactory.createChecksumMd5());
		return mediaFile;
	}

}
