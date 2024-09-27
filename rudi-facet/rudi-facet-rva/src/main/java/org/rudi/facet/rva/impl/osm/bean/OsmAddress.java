/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.osm.bean;

import java.util.List;

import lombok.Data;

/**
 * 
 * { ; ; ; ; ; ; ], ; ; ; ; [ -1.5968448, 48.1248297 ], [ -1.5968944, 48.1248468 ], [ -1.5971393, 48.1249313 ], [ -1.5972891, 48.1249079 ], [
 * -1.5978147, 48.1247466 ], [ -1.5980261, 48.124782 ], [ -1.5980844, 48.1247917 ], [ -1.5983442, 48.1248351 ], [ -1.598481, 48.1248594 ], [
 * -1.598499, 48.1248064 ] ], ; }, ; ; ; ; ; ; ; ; ; ; }
 * 
 * @author FNI18300
 *
 */
@Data
public class OsmAddress {

	private String addresstype;

	private List<Double> boundingbox;

	private String category;

	private String display_name;

	private OsmGeojson geojson;

	private Double importance;

	private String lat;

	private String licence;

	private String lon;

	private String name;

	private String osm_id;

	private String osm_type;

	private String place_id;

	private Integer place_rank;

	private String type;
}
