package org.rudi.microservice.strukture.service.helper;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.criteria.ProviderSearchCriteria;
import org.rudi.microservice.strukture.service.mapper.NodeProviderMapper;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderCustomDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProviderHelper {

	@Value("${rudi.default.contact.code:CONTACT}")
	private String codeContact;
	private final ProviderCustomDao providerCustomDao;
	private final NodeProviderUserHelper nodeProviderUserHelper;
	private final ACLHelper aclHelper;
	private final UtilPageable utilPageable;
	private final NodeProviderMapper nodeProviderMapper;

	public ProviderEntity getMyProvider() throws AppServiceUnauthorizedException {
		return getProviderFromUser(aclHelper.getAuthenticatedUser());
	}

	public NodeProvider getMyNodeProvider() throws AppServiceUnauthorizedException {
		return nodeProviderUserHelper.getNodeProviderFromUser(aclHelper.getAuthenticatedUser());
	}

	public ProviderEntity getProviderFromUser(User user) throws AppServiceUnauthorizedException {
		// Vérifie que l'utilsiteur connecté à les bons rôles
		if (!(user.getType().equals(UserType.ROBOT)
				&& user.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER)))) {
			throw new AppServiceUnauthorizedException("L'utilisateur connecté n'est pas lié au provider.");
		}

		// Vérifie que l'utilisateur connecté est bien un utilisateur d'un node provider
		NodeProvider nodeProvider = nodeProviderUserHelper.getNodeProviderFromUser(user);
		if (nodeProvider == null) {
			throw new InvalidParameterException("Node introuvable");
		}

		return getProviderFromNodeProvider(nodeProvider);
	}

	public ProviderEntity getProviderFromNodeProvider(NodeProvider nodeProvider) {
		ProviderSearchCriteria criteria = new ProviderSearchCriteria();
		criteria.setNodeProviderUuid(List.of(nodeProvider.getUuid()));
		criteria.setFull(true);
		Pageable pageable = utilPageable.getPageable(0, 1, null);

		Page<ProviderEntity> providers = providerCustomDao.searchProviders(criteria, pageable);
		if (!providers.hasContent()) {
			throw new InvalidParameterException("Provider introuvable");
		}

		return providers.getContent().get(0);
	}

	public String getContactEmail(NodeProvider nodeProvider) {
		ProviderEntity providerEntity = getProviderFromNodeProvider(nodeProvider);
		EmailAddressEntity addressEntity = (EmailAddressEntity) providerEntity.getAddresses().stream()
				.filter(addresse -> AddressType.EMAIL.equals(addresse.getAddressRole().getType())
						&& codeContact.equals(addresse.getAddressRole().getCode()))
				.findFirst().orElse(null);
		if (addressEntity == null) {
			return null;
		}
		return addressEntity.getEmail();
	}

	private Page<ProviderEntity> searchOrganizationsProviders(UUID organizationUuid, Pageable pageable, boolean full) {
		ProviderSearchCriteria criteria = ProviderSearchCriteria.builder().organisationUuid(List.of(organizationUuid)).full(full).build();

		return providerCustomDao.searchProviders(criteria, pageable);
	}

	public List<NodeProvider> getOrganizationsNodeProviders(UUID organizationUuid){
		Page<ProviderEntity> providers = searchOrganizationsProviders(organizationUuid, Pageable.unpaged(), true);
		List<NodeProviderEntity> nodeProviders = new ArrayList<>();

		for (ProviderEntity providerEntity : providers) {
			for (NodeProviderEntity nodeProviderEntity : providerEntity.getNodeProviders()) {
				if(!nodeProviders.contains(nodeProviderEntity)){
					nodeProviders.add(nodeProviderEntity);
				}
			}
		}

		return nodeProviderMapper.entitiesToDto(nodeProviders);
	}


}
