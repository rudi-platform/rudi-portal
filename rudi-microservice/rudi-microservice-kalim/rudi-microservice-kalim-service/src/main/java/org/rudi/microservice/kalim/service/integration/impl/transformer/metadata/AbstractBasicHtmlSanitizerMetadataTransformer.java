package org.rudi.microservice.kalim.service.integration.impl.transformer.metadata;

import org.rudi.common.service.util.SanitizerUtils;

public abstract class AbstractBasicHtmlSanitizerMetadataTransformer<T> extends AbstractMetadataTransformer<T> {

	protected AbstractBasicHtmlSanitizerMetadataTransformer() {
		this.sanitizerUtils = new SanitizerUtils();
	}

	private SanitizerUtils sanitizerUtils;

	protected String cleanupText(String text) {
		return sanitizerUtils.cleanupText(text);
	}

	protected String cleanupHtml(String html) {
		return sanitizerUtils.cleanupHtml(html);
	}

}
