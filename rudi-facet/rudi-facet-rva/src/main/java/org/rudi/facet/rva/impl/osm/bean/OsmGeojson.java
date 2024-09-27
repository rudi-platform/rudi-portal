/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.osm.bean;

import java.util.List;

import lombok.Data;

/**
 * @author FNI18300
 *
 */
@Data
public class OsmGeojson {

	private String type;

	private List<List<Double>> coordinates;
}
