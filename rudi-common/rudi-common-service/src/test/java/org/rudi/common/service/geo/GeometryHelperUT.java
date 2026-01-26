/**
 * RUDI Portail
 */
package org.rudi.common.service.geo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.rudi.common.service.CommonServiceSpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author FNI18300
 *
 */
@CommonServiceSpringBootTest
class GeometryHelperUT {

	@Autowired
	private GeometryHelper geometryHelper;

	@Test
	void testGeoJson1() throws IOException, URISyntaxException {
		String file = loadFile("geojson-multipoint.json");

		Geometry geometry = geometryHelper.convertGeometryFromGeoJson(file, 4326, false);
		assertNotNull(geometry);
		JSONObject jsonObject = geometryHelper.convertGeometryToGeoJson(geometry);
		assertNotNull(jsonObject);
		String geojson = jsonObject.toJSONString();
		System.out.println(geojson);
	}

	@Test
	void testGeoJson2() throws IOException, URISyntaxException {
		String file = loadFile("geojson-point.json");

		Geometry geometry = geometryHelper.convertGeometryFromGeoJson(file, 4326, false);
		assertNotNull(geometry);
		JSONObject jsonObject = geometryHelper.convertGeometryToGeoJson(geometry);
		assertNotNull(jsonObject);
		String geojson = jsonObject.toJSONString();
		System.out.println(geojson);
	}

	protected String loadFile(String name) throws IOException {
		InputStream istream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		int size = istream.available();
		byte[] buffer = new byte[size];
		IOUtils.readFully(istream, buffer);
		return new String(buffer);
	}
}
