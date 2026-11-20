package org.rudi.microservice.acl.service.helper.externalaccountrequest;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.bpmn.helper.workflow.AbstractAssignmentHelper;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestEntity;
import org.springframework.stereotype.Component;

/**
 * Helper de calcul des assignés (animateurs) pour le workflow de demande de création de compte externe
 */
@Component
public class ExternalAccountRequestAssignmentHelper extends AbstractAssignmentHelper<ExternalAccountRequestEntity> {

	@Override
	public List<String> computeAssignees(ExternalAccountRequestEntity assetDescription, String roleCode) {
		List<User> users = getAclHelper().searchUsers(roleCode);
		return users.stream().map(User::getLogin).collect(Collectors.toList());
	}

	@Override
	public String computeAssignee(ExternalAccountRequestEntity assetDescription, String roleCode) {
		List<String> assignees = computeAssignees(assetDescription, roleCode);
		if (CollectionUtils.isNotEmpty(assignees)) {
			return assignees.get(0);
		} else {
			return null;
		}
	}

}

