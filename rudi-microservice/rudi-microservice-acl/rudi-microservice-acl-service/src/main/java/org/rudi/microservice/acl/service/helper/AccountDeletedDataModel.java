package org.rudi.microservice.acl.service.helper;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Modèle de données pour les mails informant de la suppression d'un compte (utilisateur et animateurs)
 */
public class AccountDeletedDataModel extends AbstractACLTemplateDataModel {

	private final UUID userUuid;

	public AccountDeletedDataModel(UUID userUuid, Map<String, Object> additionalProperties, Locale locale,
			String template) {
		super(additionalProperties, locale, template);
		this.userUuid = userUuid;
	}

	@Override
	protected void fillDataModel(Map<String, Object> data) {
		super.fillDataModel(data);
		if (userUuid != null) {
			data.put("userUuid", userUuid.toString());
		}
	}
}
