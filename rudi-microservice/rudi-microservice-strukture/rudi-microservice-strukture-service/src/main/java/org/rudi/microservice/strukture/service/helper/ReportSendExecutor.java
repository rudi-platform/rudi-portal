package org.rudi.microservice.strukture.service.helper;

import java.util.UUID;

import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Report;



public class ReportSendExecutor implements Runnable {

	private final ReportHelper reportHelper;
	private final Report report;
	private final NodeProvider nodeProvider;
	private final int numberOfAttempts;
	private final UUID organizationUuid;

	public ReportSendExecutor(ReportHelper reportHelper, Report report, NodeProvider nodeProvider, int numberOfAttempts, UUID organizationUuid) {
		this.reportHelper = reportHelper;
		this.report = report;
		this.nodeProvider = nodeProvider;
		this.numberOfAttempts = numberOfAttempts;
		this.organizationUuid = organizationUuid;
	}

	@Override
	public void run() {
		int i = 0;
		boolean isOK = false;
		String url = reportHelper.getNodeProviderReportUrl(nodeProvider, organizationUuid);

		while (i < numberOfAttempts && !isOK) {
			i++;
			try {
				reportHelper.sendReport(report, url);
				isOK = true;
			}catch (Exception e) {
				isOK = false;
				if(i == numberOfAttempts) {
					throw e;
				}
			}
		}
	}
}
