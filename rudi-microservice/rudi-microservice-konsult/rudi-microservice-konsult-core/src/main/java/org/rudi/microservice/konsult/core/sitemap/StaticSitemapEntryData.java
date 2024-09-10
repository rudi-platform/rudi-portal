package org.rudi.microservice.konsult.core.sitemap;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class StaticSitemapEntryData extends SitemapEntryData {
	List<StaticSitemapEntry> urlList;

	@Override
	public UrlListTypeData getType() {

		return UrlListTypeData.STATICS;
	}

}
