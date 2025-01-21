/**
 * RUDI
 */
package org.rudi.microservice.acl.core.bean;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString
public class TokenSearchCritera {
	private List<TokenType> types;

	private String token;

	private String userId;
}
