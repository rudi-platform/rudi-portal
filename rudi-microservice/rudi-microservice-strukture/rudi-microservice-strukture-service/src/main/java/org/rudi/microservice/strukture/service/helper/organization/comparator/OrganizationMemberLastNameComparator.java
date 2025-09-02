package org.rudi.microservice.strukture.service.helper.organization.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.Strings;
import org.rudi.microservice.strukture.core.bean.OrganizationUserMember;

public class OrganizationMemberLastNameComparator implements Comparator<OrganizationUserMember> {

	@Override
	public int compare(OrganizationUserMember one, OrganizationUserMember two) {
		return Strings.CI.compare(one.getLastname(), two.getLastname());
	}
}
