/**
 * RUDI
 */
package org.rudi.common.facade.config.filter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * Cette extension de nimbus permet de gérer des connections à des API-M utilisant des certificats auto-signés Utilisé notamment pour les tests
 * 
 * @author FNI18300
 *
 */
@Slf4j
public class JwkResourceRetriever extends DefaultResourceRetriever {

	private static final MediaType APPLICATION_JWK_SET_JSON = new MediaType("application", "jwk-set+json");

	private static final String SSL_PROTOCOL = "TLSv1.2";

	private boolean initialized = false;

	private boolean hostVerifier = true;

	/**
	 * 
	 * @param restOperations
	 * @param connectTimeout
	 * @param readTimeout
	 * @param sizeLimit
	 * @param disconnectAfterUse
	 * @param hostVerifier
	 */
	public JwkResourceRetriever(int connectTimeout, int readTimeout, int sizeLimit, boolean disconnectAfterUse,
			boolean hostVerifier) {
		super(connectTimeout, readTimeout, sizeLimit, disconnectAfterUse);
		this.hostVerifier = hostVerifier;
	}

	@Override
	protected HttpURLConnection openHTTPConnection(URL url) throws IOException {
		initHostVerifier();

		HttpURLConnection connection = super.openHTTPConnection(url);
		log.debug("JwkResourceRetriever.openConnection:{}", connection.getURL());

		return connection;
	}

	@Override
	public Resource retrieveResource(URL url) throws IOException {
		log.debug("JwkResourceRetriever.retrieveResource:{}", url);
		if (getHeaders() == null) {
			setHeaders(new HashMap<>());
		}
		getHeaders().put(HttpHeaders.ACCEPT,
				Arrays.asList(MediaType.APPLICATION_JSON_VALUE, APPLICATION_JWK_SET_JSON.toString()));

		return super.retrieveResource(url);
	}

	private void initHostVerifier() {
		if (!initialized && !hostVerifier) {
			try {
				SSLContext sc = SSLContext.getInstance(SSL_PROTOCOL);
				sc.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

				// Create all-trusting host name verifier
				HostnameVerifier allHostsValid = (hostname, session) -> !hostVerifier;
				// Install the all-trusting host verifier
				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

				log.debug("JwkResourceRetriever.initHostVerifier {} done", allHostsValid);
				initialized = true;
			} catch (Exception e) {
				log.warn("Failed to add host verifier", e);
			}
		}
	}

	private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return ArrayUtils.toArray();
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {
			// Surcharge de checkClientTrusted
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {
			// Surcharge de checkServerTrusted
		}

	} };

}
