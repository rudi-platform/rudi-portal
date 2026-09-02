/**
 * 
 */
package org.rudi.microservice.strukture.core.bean.criteria;

import org.rudi.microservice.strukture.core.bean.AddressType;

import lombok.Data;

/**
 * @author FNI18300
 *
 */
@Data
public class AddressRoleSearchCriteria {
	private String code;
	private AddressType type;
	private Boolean active;
}
