/**
 * RUDI Portail
 */
package org.rudi.microservice.konsult.core.sitemap;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Description du JSON permettant de décrire les éléments à embarquer dans le sitemap.xml
 *
 * @author PFO23835
 */
@Data
public class SitemapDescriptionData implements Serializable {

	private static final long serialVersionUID = -4543692429285168416L;

	private int maxUrlCount;
	private int maxUrlSize;
	private StaticSitemapEntryData staticSitemapEntries;
	private List<SitemapEntryData> sitemapEntries;
}
