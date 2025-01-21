/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.service.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.facet.crypto.MediaCipherOperator;
import org.rudi.facet.crypto.RudiAlgorithmSpec;
import org.rudi.microservice.apigateway.service.ApigatewaySpringBootTest;
import org.rudi.microservice.apigateway.service.encryption.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author FNI18300
 *
 */
@ApigatewaySpringBootTest
@ActiveProfiles(profiles = { "test", "${spring.profiles.test:test-env}", "encryption" })
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class EncryptDecryptUtil {

	@Autowired
	private EncryptionService encryptionService;

	@Autowired
	private ResourceHelper resourceHelper;

	@Test
	@Disabled("This test is build to produce data to test decryption dataset")
	void encrypt_and_decrypt_ipsum_with_default_key()
			throws AppServiceException, IOException, GeneralSecurityException {
		final var spec = RudiAlgorithmSpec.DEFAULT;

		final var basefile = "ipsum.json";
		final Resource resource = resourceHelper.getResourceFromAdditionalLocationOrFromClasspath(basefile);
		final InputStream ipsum = resource.getInputStream();

		final var mediaCipherOperator = new MediaCipherOperator(spec);
		final var publicKey = encryptionService.getPublicEncryptionKey(null);

		final var encryptedOutput = new ByteArrayOutputStream();
		mediaCipherOperator.encrypt(ipsum, publicKey, encryptedOutput);

		assertThat(encryptedOutput.size()).isPositive();

		final var encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
		PrivateKey privateKey = encryptionService.getPrivateEncryptionKey(null, LocalDateTime.now());
		final var decryptedOutput = new ByteArrayOutputStream();
		mediaCipherOperator.decrypt(encryptedInput, privateKey, decryptedOutput);

		assertThat(decryptedOutput.size()).isPositive();

		try (FileOutputStream fout = new FileOutputStream("ipsum_crypted.json")) {
			fout.write(encryptedOutput.toByteArray());
		}

	}

	@Test
	@Disabled("This test is build to produce data to test decryption dataset")
	void encrypt_file_with_default_key() throws AppServiceException, IOException, GeneralSecurityException {
		// *** Paramètres à adapter *** //
		// ** fichier à déposer dans src/test/resources par exemple, le temps du chiffrement
		final var basefile = "ipsum.txt";
		String outputFilename = "ipsum_crypted.txt";
		// *** Fin paramètres à adapter *** //

		// default key configured in encryptionService
		final var publicKey = encryptionService.getPublicEncryptionKey(null);

		generateEncryptedFile(basefile, publicKey, outputFilename);
	}

	@Test
	@Disabled("This test is build to produce data to test decryption dataset with a specific public key")
	void encrypt_file_with_specific_public_key() throws AppServiceException, IOException, GeneralSecurityException {

		// *** Paramètres à adapter *** //
		// ** fichier à déposer dans src/test/resources par exemple, le temps du chiffrement
		final var basefile = "centres-de-vote.csv";
		// ** clé publique sans \r, et sans \n à la fin
		final var publicKey = buildPublicKeyFromString(
				"-----BEGIN PUBLIC KEY-----\n" + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAghPWM8uvwedCrBbOT4Zb\n"
						+ "VmVgwdI3GzT0IjsthtjReYCe3ghc17SjZvww+vGioyDOHiiTiDkvIgffV8xGWoCI\n"
						+ "V54U//2s3iGYpNH0R0s/xII2bOfzvCOawF4ZXka5nxaq/oeEfDboZw8Z/BQmlTnL\n"
						+ "4ih2qsOHPGgmWX+TNL9+s2ISCVOvkvlPw5IilSssqNotVOva14TL6eKoQsRtSqvX\n"
						+ "nXyEwManGtAo0g/j2UuPPxbgHatdfgnxUWgXn6Gaf76NTQIQ+0LycJZlWj4KysSX\n"
						+ "Kd83eaB6P0DWMEYMZv6Dr0ruykVJNoxMv8sxbcYiATK5mxIdo74X89KlmtGKE88g\n" + "RwIDAQAB\n"
						+ "-----END PUBLIC KEY-----");
		// ** Nom du fichier à generer - peut être l'UUID de media pour être deposé dans nodestub/endpoints
		final String outputFilename = "032c8a03-9968-49be-8ccf-dac417c2dd9b";
		// PENSER à COMMENTER le Disabled du test pour que le fichier soit bien généré
		// Selon la manière dont le test est lancé, le fichier est généré dans
		// rudi\rudi-microservice\rudi-microservice-apigateway\rudi-microservice-apigateway-service
		// *** Fin paramètres à adapter *** //

		generateEncryptedFile(basefile, publicKey, outputFilename);

	}

	private void generateEncryptedFile(String basefile, PublicKey publicKey, String outputFilename)
			throws GeneralSecurityException, IOException {
		final Resource resource = resourceHelper.getResourceFromAdditionalLocationOrFromClasspath(basefile);
		final InputStream ipsum = resource.getInputStream();
		final var spec = RudiAlgorithmSpec.DEFAULT;
		final var mediaCipherOperator = new MediaCipherOperator(spec);
		assertNotNull(publicKey);
		try (FileOutputStream fout = new FileOutputStream(outputFilename)) {
			mediaCipherOperator.encrypt(ipsum, publicKey, fout);
		}

	}

	private PublicKey buildPublicKeyFromString(String publicKey)
			throws InvalidKeySpecException, NoSuchAlgorithmException, UnsupportedEncodingException {

		String pubKeyPEM = publicKey.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----",
				"");

		byte[] keyBytes = Base64.decodeBase64(pubKeyPEM.getBytes("utf-8"));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(spec);

	}
}
