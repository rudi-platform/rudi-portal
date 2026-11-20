package org.rudi.facet.acl.datafactory;

import java.util.List;
import java.util.UUID;

import org.rudi.facet.acl.helper.UserSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class UserSearchCriteriaDataFactory {

	public UserSearchCriteria createForRoleUuids(List<UUID> roleUuids) {
		return UserSearchCriteria.builder().roleUuids(roleUuids).build();
	}

}
