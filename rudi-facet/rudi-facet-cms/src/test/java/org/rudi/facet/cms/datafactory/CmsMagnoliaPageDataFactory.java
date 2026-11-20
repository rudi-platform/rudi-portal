package org.rudi.facet.cms.datafactory;

import java.util.List;

import org.rudi.facet.cms.impl.model.CmsMagnoliaNode;
import org.rudi.facet.cms.impl.model.CmsMagnoliaPage;
import org.springframework.stereotype.Component;

@Component
public class CmsMagnoliaPageDataFactory {

	public <T extends CmsMagnoliaNode> CmsMagnoliaPage<T> createCmsMagnoliaPage(long total, List<T> results) {
		CmsMagnoliaPage<T> cmsMagnoliaPage = new CmsMagnoliaPage<>();
		cmsMagnoliaPage.setTotal(total);
		cmsMagnoliaPage.setResults(results);
		return cmsMagnoliaPage;
	}

}
