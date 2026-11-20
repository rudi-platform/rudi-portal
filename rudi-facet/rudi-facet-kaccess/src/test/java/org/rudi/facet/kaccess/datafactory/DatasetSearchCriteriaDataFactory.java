package org.rudi.facet.kaccess.datafactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.rudi.facet.kaccess.bean.DatasetSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class DatasetSearchCriteriaDataFactory {

	public DatasetSearchCriteria createForGlobalIdFreeTextThemeKeywordsProducerNameAndDates(UUID globalId,
			String freeText, String theme, List<String> keywords, String producerName, OffsetDateTime dateDebut,
			OffsetDateTime dateFin) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria().offset(0).limit(100)
				.globalIds(List.of(globalId)).freeText(freeText).addThemesItem(theme)
				.addProducerNamesItem(producerName).dateDebut(dateDebut).dateFin(dateFin);
		keywords.forEach(criteria::addKeywordsItem);
		return criteria;
	}

	public DatasetSearchCriteria createForFreeText(String freeText) {
		return new DatasetSearchCriteria().offset(0).limit(100).freeText(freeText);
	}

	public DatasetSearchCriteria createForGlobalIdsWithPagination(List<UUID> globalIds) {
		return new DatasetSearchCriteria().offset(0).limit(100).globalIds(globalIds);
	}

	public DatasetSearchCriteria createForKeywords(List<String> keywords) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria().offset(0).limit(100);
		keywords.forEach(criteria::addKeywordsItem);
		return criteria;
	}

	public DatasetSearchCriteria createForThemes(List<String> themes) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria().offset(0).limit(100);
		themes.forEach(criteria::addThemesItem);
		return criteria;
	}

	public DatasetSearchCriteria createForProducerNames(List<String> producerNames) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria().offset(0).limit(100);
		producerNames.forEach(criteria::addProducerNamesItem);
		return criteria;
	}

	public DatasetSearchCriteria createForOffsetLimit(int offset, int limit) {
		return new DatasetSearchCriteria().offset(offset).limit(limit);
	}

	public DatasetSearchCriteria createForDoi(String doi) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria();
		criteria.doi(doi);
		return criteria;
	}

	public DatasetSearchCriteria createForLocalId(String localId) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria();
		criteria.localId(localId);
		return criteria;
	}

	public DatasetSearchCriteria createForGlobalIds(List<UUID> globalIds) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria();
		criteria.globalIds(globalIds);
		return criteria;
	}

	public DatasetSearchCriteria createForLimitAndProducerUuids(int limit, List<UUID> producerUuids) {
		final DatasetSearchCriteria criteria = new DatasetSearchCriteria();
		criteria.setLimit(limit);
		producerUuids.forEach(criteria::addProducerUuidsItem);
		return criteria;
	}

}
