package org.rudi.microservice.acl.core.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author MCY12700
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSearchCriteria {

	private Boolean active;

	private String code;

	private String label;

}
