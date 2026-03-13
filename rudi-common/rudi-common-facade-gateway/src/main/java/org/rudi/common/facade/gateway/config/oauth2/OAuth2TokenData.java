/**
 * RUDI Portail
 */
package org.rudi.common.facade.gateway.config.oauth2;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author FNI18300
 *
 */
@Data
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

	private String type;

	private String firstname;

	private String lastname;

	private String email;

	private int errorCode;
}
