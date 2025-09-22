package org.rudi.microservice.konsult.service.mapper.jsonld.impl;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.kaccess.bean.Language;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataGeography;
import org.rudi.facet.kaccess.bean.MetadataGeographyBoundingBox;
import org.rudi.facet.kaccess.bean.RichDictionaryEntry;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.AbstractJsonLdMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.konsult.service.helper.sitemap.SitemapUtils.normalize;

@Component
public class DatasetJsonLdMapper extends AbstractJsonLdMapper<Metadata> {

	@Value("${front.urlCatalog:/catalogue/detail/}")
	private String catalogueUrlPrefixe;

	public DatasetJsonLdMapper(MediaUrlHelper mediaUrlHelper) {
		super(mediaUrlHelper);
	}

	@Override
	public JsonObject toJsonLd(Metadata metadata, DcatJsonLdContext context) throws AppServiceException {
		JsonObject result = new JsonObject();

		context.setCurentMetadata(metadata);

		JsonArray keywords = new JsonArray();
		metadata.getKeywords().forEach(keywords::add);
		context.setKeywords(keywords);

		result.addProperty("@id", metadata.getGlobalId().toString());
		result.addProperty("@type", "Dataset");
		result.addProperty("identifier", metadata.getGlobalId().toString());
		result.addProperty("title", metadata.getResourceTitle());
		result.addProperty("description", getDescription(metadata.getSummary()));
		result.addProperty("issued", metadata.getDatasetDates().getCreated().toString());
		result.addProperty("modified", metadata.getDatasetDates().getUpdated().toString());
		result.addProperty("accrualPeriodicity", "irregular");
		result.addProperty("landingPage", getDatasetUrl(metadata));

		if(!keywords.isEmpty()){
			result.add("keyword", keywords);
		}

		JsonObject spatial = getSpatialObject(metadata);
		if(spatial != null){
			result.add("spatial", spatial);
		}

		JsonArray themes = new JsonArray();
		themes.add(metadata.getTheme());
		result.add("dcat:theme", themes);

		JsonObject currentDatasetId = new JsonObject();
		currentDatasetId.addProperty("@id", result.get("@id").getAsString());
		context.setCurrentDatasetReference(currentDatasetId);

		return result;
	}

	private String getDescription(List<RichDictionaryEntry> richDictionaryEntries) {
		if(CollectionUtils.isEmpty(richDictionaryEntries)) {
			return "";
		}

		Optional<RichDictionaryEntry> frenchEntry = richDictionaryEntries
				.stream()
				.filter(summary -> summary.getLang().equals(Language.FR) ||
								summary.getLang().equals(Language.FR_FR))
				.findFirst();
		if (frenchEntry.isPresent()) {
			return getFilledEntry(frenchEntry.get());
		}

		Optional<RichDictionaryEntry> englishEntry = richDictionaryEntries
				.stream()
				.filter(summary -> summary.getLang().equals(Language.EN) ||
						summary.getLang().equals(Language.EN_GB) ||
						summary.getLang().equals(Language.EN_US))
				.findFirst();

		if(englishEntry.isPresent()){
			return getFilledEntry(englishEntry.get());
		}

		return getFilledEntry(richDictionaryEntries.get(0));
	}

	private String getDatasetUrl(Metadata metadata) {
		return StringUtils.join(
				Arrays.array(getBaseUrl(), catalogueUrlPrefixe, metadata.getGlobalId(), "/", normalize(metadata.getResourceTitle())));
	}

	private JsonObject getSpatialObject(Metadata metadata){
		MetadataGeography geography = metadata.getGeography();
		if(geography == null){
			return null;
		}

		MetadataGeographyBoundingBox bb = geography.getBoundingBox();
		JsonObject spatial = new JsonObject();

		float averageLongitude = (bb.getEastLongitude().floatValue() + bb.getWestLongitude().floatValue()) / 2;
		float averageLatitude= (bb.getSouthLatitude().floatValue() + bb.getNorthLatitude().floatValue()) / 2;

		spatial.addProperty("@type","dct:Location");
		spatial.addProperty("rdfs:label",metadata.getResourceTitle());
		spatial.addProperty("geo:lat", averageLatitude);
		spatial.addProperty("geo:long", averageLongitude);

		return spatial;
	}
	private String getFilledEntry(RichDictionaryEntry entry){
		if(entry == null){
			return "";
		}

		if(StringUtils.isNotEmpty(entry.getHtml())){
			return entry.getHtml();
		}

		return entry.getText();
	}
}
