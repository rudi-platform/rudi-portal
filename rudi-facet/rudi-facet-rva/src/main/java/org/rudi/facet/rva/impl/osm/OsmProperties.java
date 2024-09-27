package org.rudi.facet.rva.impl.osm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * https://nominatim.openstreetmap.org/search.php?street=belle épine&city=cesson
 * sévigné&county=France&polygon_geojson=1&viewbox=1.51863,47.02888,1.52625,47.02681&countrycodes=fr&limit=2&format=jsonv2
 * 
 * https://nominatim.openstreetmap.org/details.php?osmtype=W&osmid=981192300&addressdetails=1&hierarchy=0&group_hierarchy=1&polygon_geojson=1&format=json
 * 
 * @author FNI18300
 *
 */
@Configuration
@ConfigurationProperties(prefix = "rudi.osm")
@Getter
@Setter
public class OsmProperties {

	private int queryMinLength = 3;

	/**
	 * URL de l'API
	 */
	private String url = "https://nominatim.openstreetmap.org";

	private String searchPath = "/search";

	private String getPath = "/lookup";

	/**
	 * Système de réference
	 */
	private String epsg = "4326";

	private String format = "jsonv2";

	private String countrycodes = "fr";

}
