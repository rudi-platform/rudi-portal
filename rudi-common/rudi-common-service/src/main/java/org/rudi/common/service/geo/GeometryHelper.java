/**
 * RUDI Portail
 */
package org.rudi.common.service.geo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.geojson.GeoJSONDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.rudi.common.service.exception.GeometryException;
import org.rudi.common.service.url.InMemoryURLFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @author FNI18300
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GeometryHelper {

	public static final String WKT_4326 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
	public static final String WKT_3857 = "PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0 +lon_0=0 +x_0=0 +y_0=0 +k=1 +units=m +nadgrids=@null +wktext +no_defs\"],AUTHORITY[\"EPSG\",\"3857\"]]";
	public static final String WKT_32632 = "PROJCS[\"WGS 84 / UTM zone 32N\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",9],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],AUTHORITY[\"EPSG\",\"32632\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]";
	public static final String EPSG_4326 = "EPSG:4326";

	private final ObjectMapper objectMapper;

	/**
	 * Extraction de la géométrie(+srid) à partir de sa représentation WKT
	 *
	 * @param wktGeometry
	 * @return
	 */
	public Geometry convertGeometryFromWkt(String wktGeometry, int srid) {
		if (wktGeometry == null) {
			return null;
		}
		WKTReader reader = new WKTReader();
		try {
			Geometry geometry = reader.read(wktGeometry);
			geometry.setSRID(srid);
			return geometry;
		} catch (Exception e) {
			log.warn("Failed to convert geometry from apicadastre " + wktGeometry, e);
			return null;
		}
	}

	public String convertGeometryToWkt(Geometry geometry) {
		if (geometry == null) {
			return null;
		}

		WKTWriter writer = new WKTWriter();
		try {
			return writer.write(geometry);
		} catch (Exception e) {
			log.warn("Failed to convert geometry to wkt");
			return null;
		}
	}

	public Geometry convertGeometryFromGeoJson(String geojson, int srid, boolean makeValid) throws IOException {
		if (geojson == null) {
			return null;
		}
		Geometry result = null;
		if (geojson.indexOf("FeatureCollection") == -1) {
			geojson = "{\"type\": \"FeatureCollection\",\"features\": [" + geojson + "]}";
		}
		Map<String, Object> params = new HashMap<>();
		params.put(GeoJSONDataStoreFactory.URLP.key,
				InMemoryURLFactory.getInstance().build("geojson/" + UUID.randomUUID() + ".json", geojson));
		DataStore newDataStore = DataStoreFinder.getDataStore(params);
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
		SimpleFeatureCollection collection = featureSource.getFeatures();
		if (collection.size() > 0) {
			result = computeGeometryUnion(collection.features(), makeValid);
			result.setSRID(srid);
		}
		return result;
	}

	public Geometry convertGeometryFromGeoJson(JSONObject geometry, int srid) {
		String type = geometry.getAsString("type");
		Pattern regex1 = Pattern.compile("(,(?=[\\d-]))");
		Matcher m = regex1.matcher(geometry.getAsString("coordinates"));
		String coordinates = m.replaceAll(" ").replaceAll("(\\d),\\s*(\\d|\\-)", "$1 $2").replace("],[", ",")
				.replace("[", "(").replace("]", ")").replaceFirst("\\(\\(", "(").replaceFirst("\\)\\)$", ")");

		String wktGeometry = type + coordinates;

		return convertGeometryFromWkt(wktGeometry, srid);
	}

	public JSONObject convertGeometryToGeoJson(Geometry geometry) throws IOException {
		GeometryJSON geometryJSON = new GeometryJSON();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		geometryJSON.write(geometry, baos);
		JSONObject geojsonGeometry = objectMapper.reader().readValue(baos.toString(StandardCharsets.UTF_8),
				JSONObject.class);
		JSONObject feature = new JSONObject();
		feature.appendField("type", "Feature");
		feature.appendField("geometry", geojsonGeometry);
		JSONArray features = new JSONArray(1);
		features.add(feature);
		JSONObject collection = new JSONObject();
		collection.appendField("type", "FeatureCollection");
		collection.appendField("features", features);

		return collection;
	}

	public Geometry convertGeometryFromWkb(String wkbGeometry) {
		if (wkbGeometry == null) {
			return null;
		}
		WKBReader reader = new WKBReader();
		try {
			return reader.read(wkbGeometry.getBytes());
		} catch (ParseException e) {
			log.warn("Failed to convert geometry from WKB", e);
		}
		return null;
	}

	public Geometry convertGeometryFromWkb(ByteBuffer wkbGeometryByteBuffer) {
		if (wkbGeometryByteBuffer == null) {
			return null;
		}
		WKBReader reader = new WKBReader();
		try {
			return reader.read(wkbGeometryByteBuffer.array());
		} catch (ParseException e) {
			log.warn("Failed to convert geometry from WKB", e);
		}
		return null;
	}

	public Double computeSurfaceWGS84(Geometry geometry) throws GeometryException {
		return computeSurface(geometry, EPSG_4326);
	}

	/**
	 * Calcule en mètre2 de la surface
	 * 
	 * @param geometry
	 * @return
	 * @throws GeometryException
	 */
	public Double computeSurface(Geometry geometry, String crsSource) throws GeometryException {
		Double surface = 0.0d;
		if (geometry != null) {
			try {// conversion sur un projection en metre
				CoordinateReferenceSystem sourceCRS;
				if (crsSource.equals("EPSG:3857")) {
					sourceCRS = CRS.parseWKT(WKT_3857);
				} else {
					sourceCRS = CRS.parseWKT(WKT_4326);
				}

				CoordinateReferenceSystem targetCRS = CRS.parseWKT(WKT_32632);

				MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
				Geometry targetGeometry = JTS.transform(geometry, transform);

				surface = targetGeometry.getArea();
			} catch (Exception e) {
				throw new GeometryException("Failed to get geometry area", e);
			}
		}
		return surface;
	}

	/**
	 * Convertie les géométries simples en géométrie multiple
	 *
	 * @param geometry
	 * @return geometry
	 */
	public Geometry convertSimpleGeometryToMulti(Geometry geometry) {
		GeometryFactory gf = new GeometryFactory();
		if (geometry instanceof Polygon) {
			return gf.createMultiPolygon(new Polygon[] { (Polygon) geometry });
		} else if (geometry instanceof LineString) {
			return gf.createMultiLineString(new LineString[] { (LineString) geometry });
		} else if (geometry instanceof Point) {
			return gf.createMultiPoint(new Point[] { (Point) geometry });
		} else {
			return geometry;
		}
	}

	/**
	 * Calcul l'union des geometries des features de l'union
	 * 
	 * @param iterator
	 * @return
	 */
	protected Geometry computeGeometryUnion(SimpleFeatureIterator iterator, boolean makeValid) {
		Geometry result = null;
		while (iterator.hasNext()) {
			SimpleFeature feature = iterator.next();
			Geometry geometry = (Geometry) feature.getDefaultGeometry();
			if (makeValid) {
				geometry = makeValid(geometry);
			}
			if (geometry != null) {
				if (result == null) {
					result = geometry;
				} else {
					result = result.union(geometry);
				}
			}
		}
		if (result instanceof Polygon) {
			GeometryFactory gf = new GeometryFactory();
			result = gf.createMultiPolygon(new Polygon[] { (Polygon) result });
		}
		return result;
	}

	public Geometry makeValid(Geometry input) {
		if (input.isValid()) {
			return input;
		}
		if (input instanceof Polygon) {
			List<Polygon> polygons = JTS.makeValid((Polygon) input, false);
			GeometryFactory gf = new GeometryFactory();
			return gf.createMultiPolygon(polygons.toArray(Polygon[]::new));
		} else if (input instanceof MultiPolygon) {
			GeometryFactory gf = new GeometryFactory();
			MultiPolygon multiPolygon = (MultiPolygon) input;
			List<Polygon> polygons = new ArrayList<>();
			for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
				Geometry n = makeValid(multiPolygon.getGeometryN(i));
				if (n != null) {
					polygons.addAll(convertToPoygons(n));
				}
			}
			if (polygons.isEmpty()) {
				return null;
			} else {
				return gf.createMultiPolygon(polygons.toArray(Polygon[]::new));
			}
		} else if (input instanceof LineString || input instanceof MultiLineString) {
			return input;
		} else {
			return null;
		}
	}

	protected List<Polygon> convertToPoygons(Geometry input) {
		List<Polygon> polygons = new ArrayList<>();
		if (input instanceof Polygon) {
			polygons.add((Polygon) input);
		} else if (input instanceof MultiPolygon) {
			MultiPolygon multiPolygon = (MultiPolygon) input;
			for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
				Geometry n = multiPolygon.getGeometryN(i);
				polygons.addAll(convertToPoygons(n));
			}
		}
		return polygons;
	}

}
