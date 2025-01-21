package org.rudi.facet.dataverse.model.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rudi.facet.dataverse.bean.SearchItemInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class SearchElements<T extends SearchItemInfo & Serializable> implements Serializable {

	private static final long serialVersionUID = -4535741024179775412L;

	@JsonProperty(value = "total_count")
	private Long total;

	@JsonProperty(value = "start")
	private Long start;

	@JsonProperty("items")
	@Valid
	private List<T> items = null;

	@JsonProperty("facets")
	@Valid
	private List<Map<String, SearchItemFacets>> facets = null;

	public SearchElements<T> total(Long total) {
		this.total = total;
		return this;
	}

	public SearchElements<T> items(List<T> items) {
		this.items = items;
		return this;
	}

	public SearchElements<T> addItemsItem(T t) {
		if (this.items == null) {
			this.items = new ArrayList<>();
		}
		this.items.add(t);
		return this;
	}

}
