package org.rudi.microservice.konsult.service.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.konsult.core.bean.Proj4Information;
import org.rudi.microservice.konsult.service.KonsultSpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class de test de MetadataService
 */
@KonsultSpringBootTest
class MapServiceServiceUT {

	@Autowired
	private MapService mapService;

	@Test
	void test_searchProjectionInformation() throws AppServiceException {
		Proj4Information proj4Information4326 = mapService.searchProjectionInformation("epsg:4326");
		Assertions.assertNotNull(proj4Information4326);
		Proj4Information proj4Information3857 = mapService.searchProjectionInformation("epsg:3857");
		Assertions.assertNotNull(proj4Information3857);
		Proj4Information proj4Information2154 = mapService.searchProjectionInformation("epsg:2154");
		Assertions.assertNotNull(proj4Information2154);
		Proj4Information proj4Information4258 = mapService.searchProjectionInformation("epsg:4258");
		Assertions.assertNotNull(proj4Information4258);
	}
}
