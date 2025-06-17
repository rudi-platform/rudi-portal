package org.rudi.microservice.strukture.service.helper;

import java.util.UUID;

import javax.net.ssl.SSLException;

import org.rudi.common.core.webclient.HttpClientHelper;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportHelper {

	private static final String REPORT_PATH_PREFIX = "catalog/v1/";
	private static final String REPORT_PATH = "organizations/%s/report";
	private final HttpClientHelper httpClientHelper;

	@Autowired
	@Qualifier("rudi_strukture_oauth2")
	private WebClient webClient;


	public void sendReport(Report report, String url) throws SSLException {


		log.info("Sending report {}\n to {}", report, url);
		webClient.put()
				.uri(url)
				//.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(report), new ParameterizedTypeReference<Report>() {
				})
				.retrieve().bodyToMono(Void.class).block();
	}

	public String getNodeProviderReportUrl(NodeProvider nodeProvider, UUID organizationUuid) {
		StringBuilder builder = new StringBuilder();

		String nodeUrl = nodeProvider.getUrl();
		builder.append(nodeUrl);

		if (!nodeUrl.endsWith("/")) {
			builder.append("/");
		}

		builder.append(REPORT_PATH);
		return String.format(builder.toString(), organizationUuid.toString());
	}
}
