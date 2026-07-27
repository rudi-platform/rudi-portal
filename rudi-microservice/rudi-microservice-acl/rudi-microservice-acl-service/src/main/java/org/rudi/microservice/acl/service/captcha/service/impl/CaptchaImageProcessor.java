package org.rudi.microservice.acl.service.captcha.service.impl;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.service.exception.ExternalServiceException;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.microservice.acl.service.captcha.config.CaptchaProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CaptchaImageProcessor extends AbstractCaptchaProcessor<String> {

	public CaptchaImageProcessor(@Qualifier("captcha_webclient") WebClient captchaWebClient,
			CaptchaProperties captchaProperties, ResourceHelper resourceHelper) {
		super(captchaWebClient, captchaProperties, resourceHelper);
	}

	@Override
	public DocumentContent generateCaptcha(String c, String t) throws ExternalServiceException {
		String json = callPiste(c, t);
		byte[] bytes = json.getBytes();

		return generateDocumentContent(bytes);
	}

	@Override
	protected String getHandledCaptchaType() {
		return "image";
	}

	@Override
	protected String getMediaType() {
		return MediaType.APPLICATION_JSON_VALUE;
	}

	@Override
	protected Class<String> getResponseClass() {
		return String.class;
	}
}
