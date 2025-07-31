package org.rudi.microservice.kalim.service.integration.impl.validator.metadata;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.rudi.facet.dataverse.fields.FieldSpec;
import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractNotEmptyDictionaryEntryListValidator
		extends AbstractMetadataValidator<List<DictionaryEntry>> {

	protected abstract FieldSpec getFieldName();

	@Override
	public Set<IntegrationRequestErrorEntity> validate(List<DictionaryEntry> dictionaryEntries) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();

		// la liste ne doit pas être vide
		if (dictionaryEntries == null || dictionaryEntries.isEmpty()) {
			String errorMessage = String.format(IntegrationError.ERR_202.getMessage(), getFieldName().getLocalName());
			IntegrationRequestErrorEntity integrationRequestError = new IntegrationRequestErrorEntity(UUID.randomUUID(),
					IntegrationError.ERR_202.getCode(), errorMessage, getFieldName().getLocalName(),
					LocalDateTime.now());

			integrationRequestsErrors.add(integrationRequestError);
			return integrationRequestsErrors;
		}

		// chaque entrée de la liste doit avoir un texte non vide une fois nettoyé des balises HTML
		for (DictionaryEntry entry : dictionaryEntries) {
			if (StringUtils.isEmpty(extractText(entry.getText()))) {
				String errorMessage = String.format(IntegrationError.ERR_202.getMessage(),
						getFieldName().getLocalName());
				IntegrationRequestErrorEntity integrationRequestError = new IntegrationRequestErrorEntity(
						UUID.randomUUID(), IntegrationError.ERR_202.getCode(), errorMessage,
						getFieldName().getLocalName(), LocalDateTime.now());

				integrationRequestsErrors.add(integrationRequestError);
			}
		}

		return integrationRequestsErrors;
	}

	private String extractText(String text) {
		if (StringUtils.isEmpty(text)) {
			return text;
		}

		// suppression de toutes les balises
		return Jsoup.clean(text, Safelist.none());

	}

}
