package org.rudi.microservice.strukture.service.helper.organization;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.bpmn.helper.workflow.AbstractAssignmentHelper;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationAssignmentHelper extends AbstractAssignmentHelper<OrganizationEntity> {


	@Override
	public List<String> computeAssignees(OrganizationEntity assetDescription, String roleCode) {
		List<User> users = getAclHelper().searchUsers(roleCode);
		return users.stream().map(User::getLogin).collect(Collectors.toList());
	}

	@Override
	public String computeAssignee(OrganizationEntity assetDescription, String roleCode) {
		List<String> assignees = computeAssignees(assetDescription, roleCode);
		if (CollectionUtils.isNotEmpty(assignees)) {
			return assignees.get(0);
		} else {
			return null;
		}
	}
}
