package org.rudi.microservice.acl.service.captcha.service.impl;

import org.apache.commons.lang3.Strings;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.microservice.acl.service.captcha.config.CaptchaProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CaptchaSoundProcessor extends AbstractCaptchaMultimediaProcessor {

	public CaptchaSoundProcessor(@Qualifier("captcha_webclient") WebClient captchaWebClient,
			CaptchaProperties captchaProperties, ResourceHelper resourceHelper) {
		super(captchaWebClient, captchaProperties, resourceHelper);
	}

	@Override
	protected boolean hasToBeUsed(String typeCaptcha) {
		return Strings.CS.equals(typeCaptcha, CAPTCHA_TYPE_SOUND);
	}
}
