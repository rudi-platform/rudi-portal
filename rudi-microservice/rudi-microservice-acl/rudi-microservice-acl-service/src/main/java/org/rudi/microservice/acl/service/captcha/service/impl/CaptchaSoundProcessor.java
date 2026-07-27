package org.rudi.microservice.acl.service.captcha.service.impl;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.service.exception.ExternalServiceException;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.microservice.acl.service.captcha.config.CaptchaProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CaptchaSoundProcessor extends AbstractCaptchaProcessor<byte[]> {

	public CaptchaSoundProcessor(@Qualifier("captcha_webclient") WebClient captchaWebClient,
			CaptchaProperties captchaProperties, ResourceHelper resourceHelper) {
		super(captchaWebClient, captchaProperties, resourceHelper);
	}

	@Override
	public DocumentContent generateCaptcha(String c, String t) throws ExternalServiceException {
		byte[] audioBytes = callPiste(c, t);

		return generateDocumentContent(audioBytes);
	}

	@Override
	protected String getHandledCaptchaType() {
		return "sound";
	}

	@Override
	protected String getMediaType() {
		return "audio/x-wav";
	}

	@Override
	protected Class<byte[]> getResponseClass() {
		return byte[].class;
	}
}
