package org.rudi.microservice.strukture.service.helper;

import java.security.InvalidParameterException;

import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.criteria.ProviderSearchCriteria;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderCustomDao;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProviderHelper {

	private final ProviderCustomDao providerCustomDao;
	private final NodeProviderUserHelper nodeProviderUserHelper;
	private final ACLHelper aclHelper;
	private final UtilPageable utilPageable;

	public ProviderEntity getMyProvider() throws AppServiceUnauthorizedException {
		return getProviderFromUser(aclHelper.getAuthenticatedUser());
	}

	public ProviderEntity getProviderFromUser(User user) throws AppServiceUnauthorizedException {
		// Vérifie que l'utilsiteur connecté à les bons rôles
		if (!(user.getType().equals(UserType.ROBOT) && user.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER)))) {
			throw new AppServiceUnauthorizedException("Utilsiateur non lié à un provider");
		}

		// Vérifie que l'utilisateur connecté est bien un utilisateur d'un node provider
		NodeProvider nodeProvider = nodeProviderUserHelper.getNodeProviderFromUser(user);
		if (nodeProvider == null) {
			throw new InvalidParameterException("Node introuvable");
		}

		ProviderSearchCriteria criteria = new ProviderSearchCriteria();
		criteria.setNodeProviderUuid(nodeProvider.getUuid());
		criteria.setFull(true);
		Pageable pageable = utilPageable.getPageable(0, 1, null);

		Page<ProviderEntity> providers = providerCustomDao.searchProviders(criteria, pageable);
		if (!providers.hasContent()) {
			throw new InvalidParameterException("Provider introuvable");
		}

		return providers.getContent().get(0);
	}
}
