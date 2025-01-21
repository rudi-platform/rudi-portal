package org.rudi.facet.dataverse.model.search;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SearchItemFacets implements Serializable {

	private static final long serialVersionUID = -4099268530131992819L;

	@JsonProperty("friendly")
	private String friendly;

	@JsonProperty("labels")
	private List<Map<String, Integer>> labels;
}
