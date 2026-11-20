package org.rudi.facet.cms.datafactory;

import java.util.List;

import org.rudi.facet.cms.impl.model.CmsRequest;
import org.springframework.stereotype.Component;

@Component
public class CmsRequestDataFactory {

	public CmsRequest createCmsRequestWithCategories(List<String> categories) {
		return CmsRequest.builder().categories(categories).build();
	}

}
