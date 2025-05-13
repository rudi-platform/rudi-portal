package org.rudi.microservice.konsult.service.map.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.proj.Projection;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.BusinessException;
import org.rudi.facet.rva.AddressService;
import org.rudi.microservice.konsult.core.bean.Bbox;
import org.rudi.microservice.konsult.core.bean.LayerInformation;
import org.rudi.microservice.konsult.core.bean.Proj4Information;
import org.rudi.microservice.konsult.service.map.MapService;
import org.rudi.rva.core.bean.Address;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapServiceImpl implements MapService {

	private final AddressService addressService;
	private final JsonResourceReader jsonResourceReader = new JsonResourceReader();

	@Value("${rudi.konsult.rva.limit:20}")
	private int rvaLimit;

	private List<LayerInformation> datasetBaseLayers;
	private List<LayerInformation> localisationBaseLayers;

	@PostConstruct
	private void initBaseLayers() throws IOException {
		this.datasetBaseLayers = jsonResourceReader.readList("map/dataset-base-layers.json", LayerInformation.class);
		this.localisationBaseLayers = jsonResourceReader.readList("map/localisation-base-layers.json",
				LayerInformation.class);
	}

	@Override
	public List<LayerInformation> getDatasetBaseLayers() {
		return this.datasetBaseLayers;
	}

	@Override
	public List<LayerInformation> getLocalisationBaseLayers() {
		return this.localisationBaseLayers;
	}

	@Override
	public List<Address> searchAddresses(String input) throws AppServiceException {
		List<Address> addresses;
		try {
			addresses = addressService.searchAddresses(input, null, rvaLimit);
		} catch (BusinessException e) {
			// Dans le module map on ne veut pas avoir de business exception sur la recherche d'adresse
			log.error(e.getMessage());
			return Collections.emptyList();
		}
		return addresses;
	}

	@Override
	public Proj4Information searchProjectionInformation(String epsgCode) throws AppServiceException {
		CRSFactory factory = new CRSFactory();
		CoordinateReferenceSystem coordinateReferenceSystem = factory.createFromName(epsgCode.toLowerCase());
		Projection epsgProjection = coordinateReferenceSystem.getProjection();

		Bbox bbox = new Bbox();
		try {
			org.opengis.referencing.crs.CoordinateReferenceSystem crs = ReferencingFactoryFinder
					.getCRSAuthorityFactory("EPSG", null).createCoordinateReferenceSystem(epsgCode);

			Extent domainOfValidity = crs.getDomainOfValidity();
			Collection<? extends GeographicExtent> geographicElements = domainOfValidity.getGeographicElements();
			if (CollectionUtils.isNotEmpty(geographicElements)) {
				GeographicBoundingBox geographicExtent = (GeographicBoundingBox) geographicElements.iterator().next();
				bbox.setSouthLatitude(BigDecimal.valueOf(geographicExtent.getSouthBoundLatitude()));
				bbox.setWestLongitude(BigDecimal.valueOf(geographicExtent.getWestBoundLongitude()));
				bbox.setNorthLatitude(BigDecimal.valueOf(geographicExtent.getNorthBoundLatitude()));
				bbox.setEastLongitude(BigDecimal.valueOf(geographicExtent.getEastBoundLongitude()));
			}
			log.info(epsgCode + " : " + domainOfValidity.getGeographicElements().toString());
		} catch (Exception e) {
			bbox.setSouthLatitude(BigDecimal.valueOf(epsgProjection.getMinLatitude()));
			bbox.setWestLongitude(BigDecimal.valueOf(epsgProjection.getMinLongitude()));
			bbox.setNorthLatitude(BigDecimal.valueOf(epsgProjection.getMaxLatitude()));
			bbox.setEastLongitude(BigDecimal.valueOf(epsgProjection.getMaxLongitude()));
			log.error("Unable to get CRS for EPSG code {}", epsgCode, e);

		}

		return new Proj4Information().proj4(coordinateReferenceSystem.getParameterString()).bbox(bbox)
				.code(coordinateReferenceSystem.getName().toUpperCase());
	}
}
