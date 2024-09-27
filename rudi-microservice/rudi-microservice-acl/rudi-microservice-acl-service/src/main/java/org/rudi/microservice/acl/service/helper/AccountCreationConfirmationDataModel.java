/**
 * 
 */
package org.rudi.microservice.acl.service.helper;

import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.rudi.microservice.acl.storage.entity.user.UserEntity;

/**
 * @author fni18300
 *
 */
public class AccountCreationConfirmationDataModel extends AbstractACLTemplateDataModel {

	private UserEntity user;

	public AccountCreationConfirmationDataModel(UserEntity user, Map<String, Object> additionalProperties,
			@NotNull Locale locale, @NotNull String model) {
		super(additionalProperties, locale, model);
		this.user = user;
	}

	@Override
	protected void fillDataModel(Map<String, Object> data) {
		super.fillDataModel(data);
		data.put("user", user);
	}

}
