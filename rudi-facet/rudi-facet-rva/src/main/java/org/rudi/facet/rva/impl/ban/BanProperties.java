package org.rudi.facet.rva.impl.ban;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "rudi.ban")
@Getter
@Setter
public class BanProperties {

	private int queryMinLength = 3;

	/**
	 * URL de l'API
	 */
	private String url = "https://api-adresse.data.gouv.fr/search/";

	/**
	 * Système de réference
	 */
	private String epsg = "4326";

}
