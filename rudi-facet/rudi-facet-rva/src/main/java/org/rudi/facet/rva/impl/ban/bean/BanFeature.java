/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.ban.bean;

import lombok.Data;

/**
 * @author FNI18300
 *
 */
@Data
public class BanFeature {

	private String type;

	private BanProperties properties;

	private BanGeometry geometry;
}
