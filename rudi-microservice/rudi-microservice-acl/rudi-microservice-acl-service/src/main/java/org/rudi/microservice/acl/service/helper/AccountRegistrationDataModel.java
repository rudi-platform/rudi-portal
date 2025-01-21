/**
 * 
 */
package org.rudi.microservice.acl.service.helper;

import java.util.Locale;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import org.rudi.microservice.acl.storage.entity.accountregistration.AccountRegistrationEntity;

/**
 * @author fni18300
 *
 */
public class AccountRegistrationDataModel extends AbstractACLTemplateDataModel {

	private AccountRegistrationEntity accountRegistration;

	public AccountRegistrationDataModel(AccountRegistrationEntity accountRegistration,
			Map<String, Object> additionalProperties, @NotNull Locale locale, @NotNull String model) {
		super(additionalProperties, locale, model);
		this.accountRegistration = accountRegistration;
	}

	@Override
	protected void fillDataModel(Map<String, Object> data) {
		super.fillDataModel(data);
		data.put("account", accountRegistration);
	}

}
