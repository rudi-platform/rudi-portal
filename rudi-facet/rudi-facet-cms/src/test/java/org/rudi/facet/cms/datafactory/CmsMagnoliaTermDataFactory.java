package org.rudi.facet.cms.datafactory;

import java.time.LocalDateTime;

import org.rudi.facet.cms.impl.model.CmsMagnoliaTerm;
import org.springframework.stereotype.Component;

@Component
public class CmsMagnoliaTermDataFactory {

	public CmsMagnoliaTerm createCmsMagnoliaTerm(String id) {
		CmsMagnoliaTerm cmsMagnoliaTerm = new CmsMagnoliaTerm();
		cmsMagnoliaTerm.setId(id);
		return cmsMagnoliaTerm;
	}

	public CmsMagnoliaTerm createCmsMagnoliaTerm(String id, LocalDateTime created, LocalDateTime lastModified) {
		CmsMagnoliaTerm cmsMagnoliaTerm = new CmsMagnoliaTerm();
		cmsMagnoliaTerm.setId(id);
		cmsMagnoliaTerm.setCreated(created);
		cmsMagnoliaTerm.setLastModified(lastModified);
		return cmsMagnoliaTerm;
	}

}
