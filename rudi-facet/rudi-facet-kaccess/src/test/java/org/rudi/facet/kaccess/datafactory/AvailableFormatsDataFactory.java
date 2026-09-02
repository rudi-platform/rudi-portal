package org.rudi.facet.kaccess.datafactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.rudi.facet.kaccess.bean.Connector;
import org.rudi.facet.kaccess.bean.ConnectorConnectorParametersInner;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.facet.kaccess.bean.ReferenceDates;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory dédiée à l'assemblage de listes available_formats.
 */
@Component
@RequiredArgsConstructor
public class AvailableFormatsDataFactory {

	private final MediaFileDataFactory mediaFileDataFactory;
	private final MediaSeriesDataFactory mediaSeriesDataFactory;
	private final ConnectorDataFactory connectorDataFactory;
	private final ChecksumDataFactory checksumDataFactory;

	/**
	 * Formats par défaut utilisés dans agri.json.
	 * // Aucun get possible
	 */
	public List<Media> createAvailableFormatsAgriDefault() {
		final List<Media> formats = new ArrayList<>();
		formats.add(mediaFileDataFactory.createMediaFilePdf());
		formats.add(mediaFileDataFactory.createMediaFileXml());
		formats.add(mediaSeriesDataFactory.createMediaSeriesStandard());
		formats.add(mediaSeriesDataFactory.createMediaSeriesStandard());
		return formats;
	}

	/**
	 * Format minimal (un media file).
	 * // Aucun get possible
	 */
	public List<Media> createAvailableFormatsSinglePdf() {
		return List.of(mediaFileDataFactory.createMediaFilePdf());
	}

	/**
	 * Format minimal pour metadata_only_mandatory_properties.
	 * // Aucun get possible
	 */
	public List<Media> createAvailableFormatsMinimal() {
		return List.of(mediaFileDataFactory.createMediaFileMinimal());
	}

	/**
	 * Reproduction de available_format/available_format_for_update.json.
	 * // Aucun get possible
	 */
	public List<Media> createAvailableFormatsForUpdate() {
		final MediaFile mediaFile = new MediaFile();
		initializeCommonMediaFileFields(mediaFile, UUID.fromString("7eafd872-5c1e-47c2-ac57-c07926fe482e"), null, null,
				null);

		final Connector connector = enrichConnector(new Connector(), "dwnl",
				"https://www.star.fr/fileadmin/PDF_perturbations/Greve_2021/Horaires_ligne_C1.pdf", null);
		mediaFile.setConnector(connector);

		mediaFile.setFileStructure("https://:@public.sig.rennesmetropole.fr/geonetwork/srv/fre/csw?service=CSW&SERVICE=CSW&outputSchema=http%3A%2F%2Fwww.isotc211.org%2F2005%2Fgmd&request=GetRecordById&namespace=xmlns%28gmd%3Dhttp%3A%2F%2Fwww.isotc211.org%2F2005%2Fgmd%29&resultType=results&VERSION=2.0.2&version=2.0.2&ElementSetName=full&id=5fa3811a-7bb8-4899-8af1-4cda386f26b0");
		mediaFile.setFileSize(50L);
		mediaFile.setFileType("text/csv");
		mediaFile.setFileEncoding("UTF-8");
		mediaFile.setChecksum(checksumDataFactory.createChecksumMd5());

		mediaFile.setMediaDates(createReferenceDatesForUpdate(
				OffsetDateTime.parse("2021-04-01T01:10:48.970Z"),
				OffsetDateTime.parse("2022-08-15T12:00:00.970Z")));

		return List.of(mediaFile);
	}

	/**
	 * Reproduction de available_format/media_connector.metadata.json.
	 * // Aucun get possible
	 */
	public List<Media> createAvailableFormatsMediaConnectorMetadata() {
		final MediaFile mediaFile = new MediaFile();
		initializeCommonMediaFileFields(mediaFile, UUID.fromString("958b8a63-8302-4aa6-ae28-42cb5a1ee192"), "file.csv",
				"Média de type FILE", "https://cdn-icons-png.flaticon.com/512/180/180855.png");

		mediaFile.setMediaDates(createReferenceDates(
				OffsetDateTime.parse("2021-02-01T00:00:00Z"),
				OffsetDateTime.parse("2021-02-02T00:00:00Z"),
				OffsetDateTime.parse("2021-02-03T00:00:00Z"),
				OffsetDateTime.parse("2021-03-12T07:00:00Z"),
				OffsetDateTime.parse("2021-04-28T00:00:00Z"),
				OffsetDateTime.parse("2021-04-29T00:00:00Z")));

		final Connector connector = enrichConnector(connectorDataFactory.createConnectorStandard(), "dwnl",
				"https://data.explore.star.fr/explore/dataset/mkt-partenaires-garages-td/download/?format=csv&timezone=Europe/Berlin&lang=fr&use_labels_for_header=true&csv_separator=%3B",
				List.of(createConnectorParameter("mime_type", "text/csv",
						ConnectorConnectorParametersInner.TypeEnum.STRING)));
		mediaFile.setConnector(connector);

		mediaFile.setFileStructure("https://data.explore.star.fr/explore/dataset/mkt-partenaires-garages-td/download/?format=csv&timezone=Europe/Berlin&lang=fr&use_labels_for_header=true&csv_separator=%3B");
		mediaFile.setFileSize(50L);
		mediaFile.setFileType("text/csv");
		mediaFile.setFileEncoding("UTF-8");
		mediaFile.setChecksum(checksumDataFactory.createChecksumMd5());

		return List.of(mediaFile);
	}

	private void initializeCommonMediaFileFields(MediaFile mediaFile, UUID mediaId, String mediaName, String mediaCaption,
			String mediaVisual) {
		mediaFile.setMediaType(Media.MediaTypeEnum.FILE);
		mediaFile.setMediaId(mediaId);
		mediaFile.setMediaName(mediaName);
		mediaFile.setMediaCaption(mediaCaption);
		mediaFile.setMediaVisual(mediaVisual);
	}

	private ReferenceDates createReferenceDates(OffsetDateTime created, OffsetDateTime validated, OffsetDateTime published,
			OffsetDateTime updated, OffsetDateTime expires, OffsetDateTime deleted) {
		final ReferenceDates mediaDates = new ReferenceDates();
		mediaDates.setCreated(created);
		mediaDates.setValidated(validated);
		mediaDates.setPublished(published);
		mediaDates.setUpdated(updated);
		mediaDates.setExpires(expires);
		mediaDates.setDeleted(deleted);
		return mediaDates;
	}

	private ReferenceDates createReferenceDatesForUpdate(OffsetDateTime created, OffsetDateTime updated) {
		return createReferenceDates(created, null, null, updated, null, null);
	}

	private ConnectorConnectorParametersInner createConnectorParameter(String key, String value,
			ConnectorConnectorParametersInner.TypeEnum type) {
		final ConnectorConnectorParametersInner connectorParameter = new ConnectorConnectorParametersInner();
		connectorParameter.setKey(key);
		connectorParameter.setValue(value);
		connectorParameter.setType(type);
		return connectorParameter;
	}

	private Connector enrichConnector(Connector connector, String interfaceContract, String url,
			List<ConnectorConnectorParametersInner> connectorParameters) {
		connector.setInterfaceContract(interfaceContract);
		connector.setUrl(url);
		connector.setConnectorParameters(connectorParameters);
		return connector;
	}
}

