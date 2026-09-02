package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.Language;
import org.rudi.facet.kaccess.bean.RichDictionaryEntry;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer des objets RichDictionaryEntry.
 * RichDictionaryEntry est une version enrichie de DictionaryEntry avec support HTML.
 */
@Component
@RequiredArgsConstructor
public class RichDictionaryEntryDataFactory {

	private RichDictionaryEntry createRichDictionaryEntry(Language language, String text) {
		RichDictionaryEntry entry = new RichDictionaryEntry();
		entry.setLang(language);
		entry.setText(text);
		return entry;
	}

	/**
	 * Crée une entrée de dictionnaire riche en français.
	 */
	public RichDictionaryEntry createRichDictionaryEntryFrench(String text) {
		return createRichDictionaryEntry(Language.FR_FR, text);
	}

	/**
	 * Crée une entrée de dictionnaire riche en anglais.
	 */
	public RichDictionaryEntry createRichDictionaryEntryEnglish(String text) {
		return createRichDictionaryEntry(Language.EN_US, text);
	}

	/**
	 * Crée une entrée de résumé français standard pour agri.json.
	 */
	public RichDictionaryEntry createSummaryFrenchAgri() {
		return createRichDictionaryEntryFrench("Exploitations agricoles avec production de biogaz \n texte sur plusieurs lignes");
	}

	/**
	 * Crée une entrée de résumé anglais pour agri.json.
	 */
	public RichDictionaryEntry createSummaryEnglishAgri() {
		return createRichDictionaryEntryEnglish("dictionnary entry");
	}

	public RichDictionaryEntry createSummaryCsCz(){
		return createRichDictionaryEntry(Language.CS_CZ, "String");
	}
}
