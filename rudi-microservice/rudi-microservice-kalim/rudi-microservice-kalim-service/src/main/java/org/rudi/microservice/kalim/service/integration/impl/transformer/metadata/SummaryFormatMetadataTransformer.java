package org.rudi.microservice.kalim.service.integration.impl.transformer.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.RichDictionaryEntry;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SummaryFormatMetadataTransformer
		extends AbstractBasicHtmlSanitizerMetadataTransformer<List<RichDictionaryEntry>> {

	@Override
	protected List<RichDictionaryEntry> getMetadataElementToTransform(Metadata metadata) {
		return metadata.getSummary();
	}

	@Override
	protected void updateMetadata(Metadata metadata, List<RichDictionaryEntry> newValue) {
		metadata.setSummary(newValue);
	}

	@Override
	public List<RichDictionaryEntry> tranform(List<RichDictionaryEntry> summaries) {
		if (CollectionUtils.isEmpty(summaries)) {
			return Collections.emptyList();
		}

		List<RichDictionaryEntry> transformedSummaries = new ArrayList<>();

		for (RichDictionaryEntry dictionaryEntry : summaries) {
			RichDictionaryEntry transformedEntry = new RichDictionaryEntry();
			transformedEntry.setLang(dictionaryEntry.getLang());
			transformedEntry.setText(cleanupText(dictionaryEntry.getText())); // Retire toute forme de HTML
			transformedEntry.setHtml(cleanupHtml(dictionaryEntry.getHtml()));
			transformedSummaries.add(transformedEntry);
		}
		return transformedSummaries;
	}

}
