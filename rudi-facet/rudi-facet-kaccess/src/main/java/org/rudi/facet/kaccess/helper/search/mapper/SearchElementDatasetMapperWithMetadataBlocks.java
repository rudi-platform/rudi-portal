package org.rudi.facet.kaccess.helper.search.mapper;

import org.rudi.facet.dataverse.bean.DatasetMetadataBlock;
import org.rudi.facet.dataverse.bean.SearchDatasetInfo;
import org.rudi.facet.kaccess.helper.dataset.metadatablock.MetadataBlockHelper;

public class SearchElementDatasetMapperWithMetadataBlocks extends SearchElementDatasetMapper {

	public SearchElementDatasetMapperWithMetadataBlocks(MetadataBlockHelper metadataBlockHelper) {
		super(metadataBlockHelper);
	}

	@Override
	protected DatasetMetadataBlock getDatasetMetadataBlock(SearchDatasetInfo searchDatasetInfo) {
		return searchDatasetInfo.getMetadataBlocks();
	}
}
