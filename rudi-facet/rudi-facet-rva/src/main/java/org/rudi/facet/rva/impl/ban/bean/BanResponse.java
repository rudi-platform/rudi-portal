package org.rudi.facet.rva.impl.ban.bean;

import java.util.List;

import lombok.Data;

/**
 * { "type":"FeatureCollection", "version":"draft", "features":[ { "type":"Feature", "geometry":{ "type":"Point", "coordinates":[ 2.290084, 49.897443
 * ] }, "properties":{ "label":"8 Boulevard du Port 80000 Amiens", "score":0.49159121588068583, "housenumber":"8", "id":"80021_6590_00008",
 * "type":"housenumber", "name":"8 Boulevard du Port", "postcode":"80000", "citycode":"80021", "x":648952.58, "y":6977867.25, "city":"Amiens",
 * "context":"80, Somme, Hauts-de-France", "importance":0.6706612694243868, "street":"Boulevard du Port" } } ], "attribution":"BAN", "licence":"ODbL
 * 1.0", "query":"8 bd du port", "limit":1 }
 * 
 * @author FNI18300
 *
 */
@Data
public class BanResponse {

	private String type;

	private String version;

	private String attribution;

	private String licence;

	private String query;

	private String limit;

	private List<BanFeature> features;
}
