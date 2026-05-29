package org.rudi.microservice.kalim.service.helper;

import org.springframework.stereotype.Component;

@Component
public class MimeTypeHelper {

	public static final String CRYPT_SUFFIX = "+crypt";

	/**
	 *
	 * @param mimeType mimeType brut, peut contenir "+crypt"
	 * @return mimeType tel qu'attendu pour la BDD
	 */
	public String getInitialMimeType(String mimeType) {
		if (mimeType != null && mimeType.endsWith(CRYPT_SUFFIX)) {
			return mimeType.substring(0, mimeType.length() - CRYPT_SUFFIX.length());
		}
		return mimeType;
	}
}
