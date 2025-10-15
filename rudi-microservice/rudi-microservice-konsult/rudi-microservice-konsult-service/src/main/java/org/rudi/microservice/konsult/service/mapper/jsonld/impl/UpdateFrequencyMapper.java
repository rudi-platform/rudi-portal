package org.rudi.microservice.konsult.service.mapper.jsonld.impl;

import java.util.Map;

import jakarta.validation.Valid;
import org.rudi.facet.kaccess.bean.UpdateFrequency;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateFrequencyMapper {

	private static final String DEFAULT = "irregular";
	private static final Map<String, String> CODE_TO_DATA_GOUV = Map.ofEntries(
			Map.entry("continual", "continuous"),
			Map.entry("daily", "daily"),
			Map.entry("weekly", "weekly"),
			Map.entry("fortnightly", "biweekly"),
			Map.entry("monthly", "monthly"),
			Map.entry("quarterly", "quarterly"),
			Map.entry("biannually", "semiannual"),
			Map.entry("annually", "annual"),
			Map.entry("asNeeded", DEFAULT),
			Map.entry("irregular", DEFAULT),
			Map.entry("notPlanned", DEFAULT),
			Map.entry("unknown", "unknown")
	);


	public static String toDataGouv(@Valid UpdateFrequency updateFrequency) {
		if (updateFrequency == null) {
			return DEFAULT;
		}
		return toDataGouv(updateFrequency.getValue());
	}

	private static String toDataGouv(String code) {
		return CODE_TO_DATA_GOUV.get(code);
	}
}
