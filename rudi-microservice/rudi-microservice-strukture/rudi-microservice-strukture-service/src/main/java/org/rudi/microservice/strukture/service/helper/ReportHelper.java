package org.rudi.microservice.strukture.service.helper;

import java.util.UUID;

import org.rudi.common.core.webclient.HttpClientHelper;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Report;
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

	private static final String REPORT_PATH_PREFIX = "api/v1/";
	private static final String REPORT_PATH = "organizations/%s/report";
	private final HttpClientHelper httpClientHelper;


	public void sendReport(Report report, String url) {
		WebClient client = WebClient
				.builder()
				.baseUrl(url)
				.build();
		log.info("Sending report {}\n to {}", report, url);
		client.put()
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

		if (!nodeUrl.contains(REPORT_PATH_PREFIX)) {
			builder.append(REPORT_PATH_PREFIX);
		}

		builder.append(REPORT_PATH);
		return String.format(builder.toString(), organizationUuid.toString());
	}
}
