/**
 * RUDI Portail
 */
package org.rudi.microservice.strukture.service.helper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.service.mapper.NodeProviderMapper;
import org.rudi.microservice.strukture.storage.dao.provider.NodeProviderDao;
import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * @author FNI18300
 *
 */
@Component
@RequiredArgsConstructor
public class NodeProviderUserHelper {

	@Value("#{'${nodeProvider.roles}'.split(',')}")
	private List<String> roleCodes;

	private final ACLHelper aclHelper;

	private final NodeProviderDao nodeProviderDao;

	private final NodeProviderMapper nodeProviderMapper;

	public void updateAssociatedUser(ProviderEntity provider, NodeProviderEntity nodeProvider) {
		User user = aclHelper.getUserByLogin(nodeProvider.getUuid().toString());
		if (user == null) {
			List<Role> roles = aclHelper.searchRoles();
			if (CollectionUtils.isNotEmpty(roles)) {
				roles = roles.stream().filter(role -> roleCodes.contains(role.getCode())).collect(Collectors.toList());
			}
			user = new User();
			user.setLogin(nodeProvider.getUuid().toString());
			user.setType(UserType.ROBOT);
			user.setCompany(provider.getCode());
			user.setFirstname(provider.getLabel());
			user.setRoles(roles);
			aclHelper.createUser(user);
		} else {
			user.setType(UserType.ROBOT);
			user.setCompany(provider.getCode());
			user.setFirstname(provider.getLabel());
			aclHelper.updateUser(user);
		}

	}

	public void deleteAssociatedUser(NodeProviderEntity nodeProviderEntity) {
		User user = aclHelper.getUserByLogin(nodeProviderEntity.getUuid().toString());
		if (user != null) {
			aclHelper.deleteUserByUUID(user.getUuid());
		}
	}

	public void deleteAssociatedUser(ProviderEntity providerEntity) {
		if (CollectionUtils.isNotEmpty(providerEntity.getNodeProviders())) {
			for (NodeProviderEntity nodeProviderEntity : providerEntity.getNodeProviders()) {
				deleteAssociatedUser(nodeProviderEntity);
			}
		}
	}

	public NodeProvider getNodeProviderFromUser(User user){
		NodeProviderEntity entity = nodeProviderDao.findByUuid(UUID.fromString(user.getLogin()));
		if(entity == null){
			return null;
		}
		return nodeProviderMapper.entityToDto(entity);
	}

}
