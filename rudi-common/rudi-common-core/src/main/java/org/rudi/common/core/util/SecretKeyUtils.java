/**
 * RUDI Portail
 */
package org.rudi.common.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 */
@Slf4j
public class SecretKeyUtils {

	public static final String FILE_PREFIX = "file:";

	private SecretKeyUtils() {
	}

	/**
	 * @param keyProperty
	 * @return
	 */
	public static String computeKeyFromPropery(String keyProperty) {
		String computedKey = null;
		if (StringUtils.isNotEmpty(keyProperty)) {
			boolean rsaKey = false;
			if (keyProperty.startsWith(FILE_PREFIX)) {
				computedKey = readKey(keyProperty.substring(FILE_PREFIX.length()));
				if (StringUtils.isNotEmpty(computedKey)) {
					log.info("Return signing rsa key");
					rsaKey = true;
				} else {
					log.info("Invalid rsa key");
				}
			}
			if (!rsaKey) {
				log.info("Return signing mac key");
				computedKey = keyProperty;
			}
		}
		return computedKey;
	}

	/**
	 * @param name
	 * @return
	 */
	public static String readKey(String name) {
		String content = null;
		File f = new File(name);
		if (f.exists()) {
			try (InputStream is = new FileInputStream(f)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(is, baos);
				content = baos.toString();
			} catch (Exception e) {
				log.warn("Failed to read {} from file", name);
			}
		} else {
			try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(is, baos);
				content = baos.toString();
			} catch (Exception e) {
				log.warn("Failed to read {} from classloader", name);
			}
		}
		return content;
	}

	public static KeyPair readKeyPairFromContents(String publicKeyContent, String privateKeyContent)
			throws IOException {
		final PublicKey publicKey = SecretKeyUtils.readPublicKeyFromContent(publicKeyContent);
		final PrivateKey privateKey = SecretKeyUtils.readPrivateKeyFromContent(privateKeyContent);
		return new KeyPair(publicKey, privateKey);
	}

	// Source : https://www.baeldung.com/java-read-pem-file-keys
	private static PublicKey readPublicKeyFromContent(String encodedKeyContent) throws IOException {
		final Object pemContent = parsePEMContent(encodedKeyContent);
		final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemContent);
		final JcaPEMKeyConverter converter = getConverter();
		return converter.getPublicKey(publicKeyInfo);
	}

	// Source : https://www.baeldung.com/java-read-pem-file-keys
	private static PrivateKey readPrivateKeyFromContent(String encodedKeyContent) throws IOException {
		final Object pemContent = parsePEMContent(encodedKeyContent);
		final PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemContent);
		final JcaPEMKeyConverter converter = getConverter();
		return converter.getPrivateKey(privateKeyInfo);
	}

	@Nonnull
	private static JcaPEMKeyConverter getConverter() {
		return new JcaPEMKeyConverter();
	}

	private static Object parsePEMContent(String encodedKeyContent) throws IOException {
		try (Reader keyReader = new StringReader(encodedKeyContent)) {
			PEMParser pemParser = new PEMParser(keyReader);
			return pemParser.readObject();
		}
	}
}
