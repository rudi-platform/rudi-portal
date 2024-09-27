/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.ban.bean;

import java.util.List;

import lombok.Data;

/**
 * @author FNI18300
 *
 */
@Data
public class BanGeometry {

	private String type;

	private List<Double> coordinates;
}
