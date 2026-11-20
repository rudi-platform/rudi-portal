package org.rudi.facet.cms.datafactory;

import org.rudi.facet.cms.impl.model.CmsMagnoliaCategory;
import org.springframework.stereotype.Component;

@Component
public class CmsMagnoliaCategoryDataFactory {

	public CmsMagnoliaCategory createCmsMagnoliaCategory(String id, String path, String displayName) {
		CmsMagnoliaCategory cmsMagnoliaCategory = new CmsMagnoliaCategory();
		cmsMagnoliaCategory.setId(id);
		cmsMagnoliaCategory.setPath(path);
		cmsMagnoliaCategory.setDisplayName(displayName);
		return cmsMagnoliaCategory;
	}

}
