/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.ban.bean;

import lombok.Builder;
import lombok.Data;

/**
 * @author FNI18300
 *
 */
@Data
@Builder
public class BanContext {

	private Integer limit;

	private String type;

	private String query;
}
