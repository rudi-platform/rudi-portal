package org.rudi.microservice.konsult.core.sitemap;

import java.io.Serializable;

import lombok.Data;

@Data
public class SitemapEntryData implements Serializable {

	private static final long serialVersionUID = -3370035706940183499L;

	private UrlListTypeData type;
}
