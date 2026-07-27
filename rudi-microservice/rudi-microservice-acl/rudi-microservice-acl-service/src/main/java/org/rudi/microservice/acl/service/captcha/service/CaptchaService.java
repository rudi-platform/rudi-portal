package org.rudi.microservice.acl.service.captcha.service;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.service.exception.ExternalServiceException;
import org.rudi.microservice.acl.core.bean.CaptchaModel;

public interface CaptchaService {
	/**
	 * Appelle l'API CaptchEtat v2 pour récupérer un élément du captcha.
	 *
	 * @param get  type d'objet : "image" ou "sound"
	 * @param c    nom du captcha (ex: captchaFR)
	 * @param t    identifiant UUID du captcha (requis pour sound)
	 * @return DocumentContent contenant la réponse (JSON pour image, binaire pour sound)
	 */
	DocumentContent generateCaptcha(String get, String c, String t) throws ExternalServiceException;

	/**
	 * Valide la saisie utilisateur d'un captcha auprès de l'API CaptchEtat v2.
	 *
	 * @param captchaModel DTO contenant uuid et code
	 * @return true si le captcha est validé, false sinon
	 */
	Boolean validateCaptcha(CaptchaModel captchaModel) throws ExternalServiceException;
}
