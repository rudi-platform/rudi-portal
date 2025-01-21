/**
 * RUDI Portail
 */
package org.rudi.common.facade.config.filter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * @author FNI18300
 */
@Data
@Builder
public class OAuth2TokenData {

	private List<String> scope;

	private boolean active;

	private Long exp;

	private List<String> authorities;

	private String jti;

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("user_name")
	private String userName;
}
