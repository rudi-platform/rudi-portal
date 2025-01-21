package org.rudi.facet.dataverse.api.search;

import java.net.URI;
import java.util.EnumSet;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.facet.dataverse.api.AbstractDataverseAPI;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.dataverse.bean.SearchItemInfo;
import org.rudi.facet.dataverse.bean.SearchType;
import org.rudi.facet.dataverse.model.DataverseResponse;
import org.rudi.facet.dataverse.model.search.SearchElements;
import org.rudi.facet.dataverse.model.search.SearchParams;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractSearchOperationAPI<T extends SearchItemInfo> extends AbstractDataverseAPI {

	protected AbstractSearchOperationAPI(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	public SearchElements<T> searchDataset(SearchParams searchParams) throws DataverseAPIException {
		String url = createUrl("search");
		ParameterizedTypeReference<DataverseResponse<SearchElements<T>>> type = new ParameterizedTypeReference<>() {
		};

		DataverseResponse<SearchElements<T>> resp = getWebClient().get()
				.uri(uri -> buildSearchUri(uri, url, searchParams)).retrieve().bodyToMono(type)
				.doOnError(t -> handleError("Failed to search dataset", t)).block();
		return getDataBody(resp);
	}

	protected void validateSearchParams(SearchParams searchParams, SearchType expected) {
		if (CollectionUtils.isEmpty(searchParams.getType()) || searchParams.getType().size() != 1
				|| !searchParams.getType().contains(expected)) {
			throw new IllegalArgumentException(
					String.format("Search must be configured to search only  %ss", expected.name()));
		}
	}

	private URI buildSearchUri(UriBuilder uriBuilder, String url, SearchParams searchParams) {
		uriBuilder.path(url).queryParamIfPresent("q", convertOptionalValue(searchParams.getQ()));
		EnumSet<SearchType> types = searchParams.getType();
		if (!CollectionUtils.isEmpty(types)) {
			uriBuilder.queryParam("type", types.toArray());
		}
		if (!StringUtils.isEmpty(searchParams.getSubtree())) {
			uriBuilder.queryParam("subtree", searchParams.getSubtree());
		}
		if (!CollectionUtils.isEmpty(searchParams.getFilterQuery())) {
			uriBuilder.queryParam("fq", searchParams.getFilterQuery());
		}
		if (searchParams.getSortBy() != null) {
			uriBuilder.queryParam("sort", searchParams.getSortBy());
		}
		if (searchParams.getSortOrder() != null) {
			uriBuilder.queryParam("order", searchParams.getSortOrder());
		}
		if (searchParams.getPerPage() != null && searchParams.getPerPage() != 0) {
			uriBuilder.queryParam("per_page", searchParams.getPerPage());
		}
		if (searchParams.getStart() != null && searchParams.getStart() != 0) {
			uriBuilder.queryParam("start", searchParams.getStart());
		}
		if (BooleanUtils.isTrue(searchParams.getShowFacets())) {
			uriBuilder.queryParam("show_facets", true);
		}
		if (BooleanUtils.isTrue(searchParams.getShowRelevance())) {
			uriBuilder.queryParam("show_relevance", true);
		}
		if (CollectionUtils.isNotEmpty(searchParams.getMetadatafields())) {
			uriBuilder.queryParam("metadata_fields", searchParams.getMetadatafields());
		}
		return uriBuilder.build();
	}

	protected Optional<String> convertOptionalValue(String value) {
		if (StringUtils.isNotEmpty(value)) {
			return Optional.of(value);
		} else {
			return Optional.empty();
		}
	}
}
