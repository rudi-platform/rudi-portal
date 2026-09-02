package org.rudi.facet.kaccess.datafactory;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.MediaSeries;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets MediaSeries.
 * Une MediaSeries représente un flux de données en continu.
 */
@Component
@RequiredArgsConstructor
public class MediaSeriesDataFactory {

	private static final UUID MEDIA_ID_SERIES_STANDARD = UUID.fromString("1a3b81cf-7ca4-4055-b2c6-aa8971faadc0");

	private final ConnectorDataFactory connectorDataFactory;

	/**
	 * Crée une série de données standard.
	 * Le mediaId est fixe pour garantir la stabilité des fixtures de référence.
	 */
	public MediaSeries createMediaSeriesStandard() {
		MediaSeries mediaSeries = new MediaSeries();
		mediaSeries.setMediaId(MEDIA_ID_SERIES_STANDARD);
		mediaSeries.setMediaType(MediaSeries.MediaTypeEnum.SERIES);
		mediaSeries.setConnector(connectorDataFactory.createConnectorSeries());
		mediaSeries.setLatency(100L);
		mediaSeries.setPeriod(10L);
		mediaSeries.setCurrentNumberOfRecords(20L);
		mediaSeries.setCurrentSize(328L);
		mediaSeries.setTotalNumberOfRecords(80L);
		mediaSeries.setTotalSize(500L);
		return mediaSeries;
	}

}
