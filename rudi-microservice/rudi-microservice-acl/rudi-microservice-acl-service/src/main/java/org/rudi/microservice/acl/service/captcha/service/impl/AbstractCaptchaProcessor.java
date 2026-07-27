package org.rudi.microservice.acl.service.captcha.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.service.exception.ExternalServiceException;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.common.service.util.MonoUtils;
import org.rudi.microservice.acl.service.captcha.config.CaptchaProperties;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCaptchaProcessor<T> {

	private final WebClient captchaWebClient;
	private final CaptchaProperties captchaProperties;
	private final ResourceHelper resourceHelper;

	public abstract DocumentContent generateCaptcha(String c, String t) throws ExternalServiceException;

	protected abstract String getHandledCaptchaType();

	protected MediaType getHandledMediaType() {
		return MediaType.valueOf(getMediaType());
	}

	protected abstract String getMediaType();

	protected abstract Class<T> getResponseClass();

	public boolean hasToBeUsed(String typeCaptcha) {
		return Strings.CS.equals(typeCaptcha, getHandledCaptchaType());
	}

	/**
	 * Exécute un appel GET vers PISTE et retourne le résultat en bloquant.
	 */
	protected T callPiste(String c, String t) throws ExternalServiceException {
		return MonoUtils.blockOrThrow(captchaWebClient.get()
				.uri(uriBuilder -> buildPath(uriBuilder, getHandledCaptchaType(), c, t)).accept(getHandledMediaType())
				.attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction
						.clientRegistrationId(CaptchaProperties.REGISTRATION_ID))
				.retrieve().bodyToMono(getResponseClass()), ExternalServiceException.class);
	}

	protected DocumentContent generateDocumentContent(byte[] bytes) throws ExternalServiceException {
		try {
			File file = resourceHelper.createTemporaryFile();
			FileUtils.writeByteArrayToFile(file, bytes);
			return new DocumentContent("fluxFile", getMediaType(), file.length(), file);
		} catch (IOException ioException) {
			log.error("Exception lors des traitements sur le fichier temporaire (lecture ou écriture)");
			throw new ExternalServiceException(
					"Exception lors des traitements sur le fichier temporaire (lecture ou écriture)", ioException);
		}
	}

	/**
	 * Construction de l'url d'appel de l'API de captcha en fonction des params non nuls
	 *
	 * @param builder builder de l'uri
	 * @param get     type de captcha (html, image...)
	 * @param c       param 1
	 * @param t       param 2 utilisé par le component front uniquement
	 * @return l'uri construite pour appeler l"API externe
	 */
	private URI buildPath(UriBuilder builder, String get, String c, String t) {
		builder.path(captchaProperties.getCaptchaEndpoint()).queryParam("get", get).queryParam("c", c);
		if (t != null) {
			builder.queryParam("t", t);
		}
		return builder.build();
	}

}
