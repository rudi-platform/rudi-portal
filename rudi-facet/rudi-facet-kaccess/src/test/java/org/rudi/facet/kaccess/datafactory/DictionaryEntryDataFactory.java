package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.facet.kaccess.bean.Language;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer des objets DictionaryEntry.
 * DictionaryEntry représente une entrée multilingue simple (langue + texte).
 */
@Component
@RequiredArgsConstructor
public class DictionaryEntryDataFactory {

	/**
	 * Crée une entrée de dictionnaire en français.
	 */
	public DictionaryEntry createDictionaryEntryFrench(String text) {
		return createDictionaryEntry(Language.FR_FR, text);
	}

	/**
	 * Crée une entrée de dictionnaire en anglais.
	 */
	public DictionaryEntry createDictionaryEntryEnglish(String text) {
		return createDictionaryEntry(Language.EN_US, text);
	}

	private DictionaryEntry createDictionaryEntry(Language language, String text) {
		DictionaryEntry entry = new DictionaryEntry();
		entry.setLang(language);
		entry.setText(text);
		return entry;
	}

	/**
	 * Crée une entrée de synopsis français standard.
	 */
	public DictionaryEntry createSynopsisFrench() {
		return createDictionaryEntryFrench("Résumé court en français");
	}

	/**
	 * Crée une entrée de synopsis anglais standard.
	 */
	public DictionaryEntry createSynopsisEnglish() {
		return createDictionaryEntryEnglish("English short description");
	}

}
