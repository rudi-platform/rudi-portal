package org.rudi.microservice.konsult.core.harvest;

import java.util.List;

import org.rudi.facet.kaccess.bean.DatasetSearchCriteria;
import org.rudi.facet.kaccess.bean.Metadata;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Component
public class DcatJsonLdContext {
	private Long totalDatasets;
	private DatasetSearchCriteria datasetSearchCriteria;
	private Metadata curentMetadata;
	private JsonArray keywords;
	private JsonObject license;
	private JsonArray contactPoints;

	private List<JsonObject> kinds;
	private JsonArray distributions;
	private JsonObject currentDatasetReference;
}
