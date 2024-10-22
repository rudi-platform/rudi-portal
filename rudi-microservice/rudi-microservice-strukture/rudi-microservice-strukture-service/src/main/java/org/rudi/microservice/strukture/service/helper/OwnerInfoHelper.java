package org.rudi.microservice.strukture.service.helper;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.entity.workflow.AssetDescriptionEntity;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.core.bean.OwnerType;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OwnerInfoHelper {
	@Value("${rudi.default.contact.code:CONTACT}")
	private String codeContact;
	private final ACLHelper aclHelper;
	private final ProviderHelper providerHelper;

	public OwnerInfo getAssetDescriptionOwnerInfo(AssetDescriptionEntity entity) throws IllegalArgumentException {
		User user = aclHelper.getUserByLogin(entity.getInitiator());
		if (user == null) {
			throw new IllegalArgumentException(
					String.format("No initiating user related to assetDescription %s", entity.getUuid()));
		}
		if (user.getType().equals(UserType.ROBOT) && user.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER))) {
			ProviderEntity providerEntity;
			try {
				providerEntity = providerHelper.getProviderFromUser(user);
			} catch (AppServiceUnauthorizedException e) {
				throw new IllegalArgumentException(
						String.format("No provider related to user login %s", user.getLogin()));
			}

			EmailAddressEntity addressEntity = (EmailAddressEntity) providerEntity.getAddresses().stream()
					.filter(addresse -> AddressType.EMAIL.equals(addresse.getAddressRole().getType()) && codeContact.equals(addresse.getAddressRole().getCode()))
					.findFirst().orElse(null);

			String contact = addressEntity == null || StringUtils.isEmpty(addressEntity.getEmail()) ?
					user.getLogin()
					: addressEntity.getEmail();

			return new OwnerInfo().ownerType(OwnerType.ORGANIZATION).contact(contact).name(user.getCompany());
		}
		return new OwnerInfo().ownerType(OwnerType.USER).contact(user.getLogin()).name(String.format("%s %s", user.getFirstname(), user.getLastname()));
	}
}
