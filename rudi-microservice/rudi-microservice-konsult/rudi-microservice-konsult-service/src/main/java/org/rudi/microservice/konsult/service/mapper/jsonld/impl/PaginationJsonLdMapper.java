package org.rudi.microservice.konsult.service.mapper.jsonld.impl;


import com.google.gson.JsonObject;
import org.rudi.facet.kaccess.bean.DatasetSearchCriteria;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.AbstractJsonLdMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaginationJsonLdMapper extends AbstractJsonLdMapper<DatasetSearchCriteria> {


	@Value("${harvest.dcat.default.page-size:10}")
	private int harvestDefaultPageSize;

	public PaginationJsonLdMapper(MediaUrlHelper mediaUrlHelper) {
		super(mediaUrlHelper);
	}

	@Override
	public JsonObject toJsonLd(DatasetSearchCriteria criteria, DcatJsonLdContext context) {
		JsonObject result = new JsonObject();

		int pageSize = criteria.getLimit() != null ? criteria.getLimit() :harvestDefaultPageSize;
		int page = criteria.getOffset() != null ? (criteria.getOffset() / pageSize) + 1 : 1;
		int lastPage = (int) Math.ceil((double) context.getTotalDatasets() /pageSize);


		int next = page + 1;
		int previous = page - 1;


		result.addProperty("@id", getUrl(page, pageSize));
		result.addProperty("@type", "hydra:PartialCollectionView");
		result.addProperty("first",getUrl(1, pageSize));
		result.addProperty("last",getUrl(lastPage, pageSize));

		if(next <= lastPage) {
			result.addProperty("next",getUrl(next, pageSize));
		}
		if(previous >= 1) {
			result.addProperty("previous",getUrl(previous, pageSize));
		}

		return result;
	}

	private String getUrl(int page, int pageSize){
		return String.format("%s/konsult/v1/datasets/metadatas/dcat?page=%d&pageSize=%d", getBaseUrl(), page, pageSize);
	}
}
