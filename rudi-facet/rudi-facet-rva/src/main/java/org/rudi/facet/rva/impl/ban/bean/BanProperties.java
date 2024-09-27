package org.rudi.facet.rva.impl.ban.bean;

import lombok.Data;

/**
 * "label":"8 Boulevard du Port 80000 Amiens", "score":0.49159121588068583, "housenumber":"8", "id":"80021_6590_00008", "type":"housenumber",
 * "name":"8 Boulevard du Port", "postcode":"80000", "citycode":"80021", "x":648952.58, "y":6977867.25, "city":"Amiens", "context":"80, Somme,
 * Hauts-de-France", "importance":0.6706612694243868, "street":"Boulevard du Port"
 * 
 * @author FNI18300
 *
 */
@Data
public class BanProperties {

	private String label;
	private String score;
	private String housenumber;
	private String id;
	private String type;
	private String name;
	private String postcode;
	private String citycode;
	private String x;
	private String y;
	private String city;
	private String context;
	private String importance;
	private String street;
}
