package org.rudi.microservice.acl.service.helper;

import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.rudi.microservice.acl.storage.entity.accountupdate.ResetPasswordRequestEntity;

public class ResetPasswordRequestDataModel extends AbstractACLTemplateDataModel {

	private ResetPasswordRequestEntity resetPasswordRequestEntity;

	protected ResetPasswordRequestDataModel(ResetPasswordRequestEntity passwordEntity,
			Map<String, Object> additionalProperties, @NotNull Locale locale, String model) {
		super(additionalProperties, locale, model);
		resetPasswordRequestEntity = passwordEntity;
	}

	@Override
	protected void fillDataModel(Map<String, Object> data) {
		super.fillDataModel(data);
		data.put("resetPasswordRequest", resetPasswordRequestEntity);
	}
}
