package org.rudi.microservice.konsult.core.sitemap;

import java.io.Serializable;

import lombok.Data;

@Data
public class StaticSitemapEntry implements Serializable {

	private static final long serialVersionUID = -688887436947039129L;

	private String location;
	private Boolean isRelative;

}
