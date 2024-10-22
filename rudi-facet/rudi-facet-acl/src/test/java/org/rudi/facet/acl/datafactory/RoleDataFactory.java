package org.rudi.facet.acl.datafactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.helper.ACLHelper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class RoleDataFactory extends AbstractDataFactory {
	private final ACLHelper helper;

	public Role getOrCreate(String code, @Nullable String label) {
		final List<Role> roles = helper.searchRoles();
		Optional<Role> optionalRole = roles.stream().filter(r -> r.getCode().equals(code)).findFirst();
		if(optionalRole.isPresent()) {
			return optionalRole.get();
		}
		LocalDateTime now = LocalDateTime.now();

		if(StringUtils.isBlank(label)) {
			label = code.toLowerCase().replaceAll("_"," ");
		}

		Role role = new Role();
		role.setUuid(UUID.randomUUID());
		role.setCode(code);
		role.setLabel(label);
		role.setOpeningDate(handleDate(now));

		return role;
	}
}
