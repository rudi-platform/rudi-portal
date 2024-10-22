package org.rudi.microservice.strukture.service.helper.provider;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.bpmn.helper.workflow.AbstractAssignmentHelper;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.stereotype.Component;

@Component
public class LinkedProducerAssignmentHelper extends AbstractAssignmentHelper<LinkedProducerEntity> {
	/**
	 * @param assetDescription
	 * @param roleCode
	 * @return
	 */
	@Override
	public String computeAssignee(LinkedProducerEntity assetDescription, String roleCode) {
		List<String> assignees = computeAssignees(assetDescription, roleCode);
		if (CollectionUtils.isNotEmpty(assignees)) {
			return assignees.get(0);
		} else {
			return null;
		}
	}

	/**
	 * @param assetDescription
	 * @param roleCode
	 * @return
	 */
	@Override
	public List<String> computeAssignees(LinkedProducerEntity assetDescription, String roleCode) {
		List<User> users = getAclHelper().searchUsers(roleCode);
		return users.stream().map(User::getLogin).collect(Collectors.toList());
	}
}
