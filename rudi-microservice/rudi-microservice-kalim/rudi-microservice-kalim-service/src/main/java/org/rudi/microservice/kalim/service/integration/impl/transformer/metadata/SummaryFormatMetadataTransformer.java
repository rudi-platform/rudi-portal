package org.rudi.microservice.kalim.service.integration.impl.transformer.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.facet.kaccess.bean.Metadata;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SummaryFormatMetadataTransformer
		extends AbstractBasicHtmlSanitizerMetadataTransformer<List<DictionaryEntry>> {

	@Override
	protected List<DictionaryEntry> getMetadataElementToTransform(Metadata metadata) {
		return metadata.getSummary();
	}

	@Override
	protected void updateMetadata(Metadata metadata, List<DictionaryEntry> newValue) {
		metadata.setSummary(newValue);
	}

	@Override
	public List<DictionaryEntry> tranform(List<DictionaryEntry> summaries) {
		if (CollectionUtils.isEmpty(summaries)) {
			return Collections.emptyList();
		}

		List<DictionaryEntry> transformedSummaries = new ArrayList<>();

		for (DictionaryEntry dictionaryEntry : summaries) {
			DictionaryEntry transformedEntry = new DictionaryEntry();
			transformedEntry.setLang(dictionaryEntry.getLang());
			transformedEntry.setText(cleanupText(dictionaryEntry.getText()));
			transformedSummaries.add(transformedEntry);
		}
		return transformedSummaries;
	}

}
