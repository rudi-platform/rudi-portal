/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.helper.project;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.microservice.projekt.service.helper.AbstractProjektAssigmentHelper;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Component
@Slf4j
public class ProjectAssigmentHelper extends AbstractProjektAssigmentHelper<ProjectEntity> {

	@Value("${application.role.moderator.code}")
	@Getter
	private String moderatorRoleCode;

	@Autowired
	@Getter(value = AccessLevel.PROTECTED)
	private OrganizationHelper organizationHelper;

	@Autowired
	@Getter(value = AccessLevel.PROTECTED)
	private ACLHelper aclHelper;

	@Override
	public List<String> computeAssignees(ProjectEntity assetDescription, String roleCode) {
		List<User> users = getAclHelper().searchUsers(roleCode);
		return users.stream().map(User::getLogin).collect(Collectors.toList());
	}

	@Override
	public String computeAssignee(ProjectEntity assetDescription, String roleCode) {
		List<String> assignees = computeAssignees(assetDescription, roleCode);
		if (CollectionUtils.isNotEmpty(assignees)) {
			return assignees.get(0);
		} else {
			return null;
		}
	}

}
