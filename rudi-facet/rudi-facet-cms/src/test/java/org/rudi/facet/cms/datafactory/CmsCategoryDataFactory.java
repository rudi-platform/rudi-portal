package org.rudi.facet.cms.datafactory;

import org.rudi.facet.cms.bean.CmsCategory;
import org.springframework.stereotype.Component;

@Component
public class CmsCategoryDataFactory {

	public CmsCategory createCmsCategory(String id, String path, String label) {
		CmsCategory cmsCategory = new CmsCategory();
		cmsCategory.setId(id);
		cmsCategory.setPath(path);
		cmsCategory.setLabel(label);
		return cmsCategory;
	}

	public CmsCategory createCmsCategory(String id, String path) {
		CmsCategory cmsCategory = new CmsCategory();
		cmsCategory.setId(id);
		cmsCategory.setPath(path);
		return cmsCategory;
	}

}
