package org.rudi.microservice.projekt.service.datafactory;

import org.rudi.facet.acl.bean.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleDataFactory {

	public Role createRole(String code) {
		return new Role().code(code);
	}

}
