package org.rudi.common.facade.config.filter;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FNI18300
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tokens implements Serializable {

	private static final long serialVersionUID = -3091241502302292087L;

	private String jwtToken;

	private String refreshToken;
}
