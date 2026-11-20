package org.rudi.microservice.acl.service.helper;

import java.util.Locale;
import java.util.Map;

/**
 * Modèle de données pour la génération du mail d'avertissement de suppression de compte
 */
public class AccountDeletionWarningDataModel extends AbstractACLTemplateDataModel {

	private final boolean hasPendingTasks;
	private final String userDenomination;
	private final int warningThresholdMonths;

	public AccountDeletionWarningDataModel(boolean hasPendingTasks, String userDenomination, int warningThresholdMonths,
			Map<String, Object> additionalProperties, Locale locale, String template) {
		super(additionalProperties, locale, template);
		this.hasPendingTasks = hasPendingTasks;
		this.userDenomination = userDenomination;
		this.warningThresholdMonths = warningThresholdMonths;
	}

	@Override
	protected void fillDataModel(Map<String, Object> data) {
		super.fillDataModel(data);
		data.put("hasPendingTasks", hasPendingTasks);
		data.put("userDenomination", userDenomination);
		data.put("warningThresholdMonths", warningThresholdMonths);
	}
}

