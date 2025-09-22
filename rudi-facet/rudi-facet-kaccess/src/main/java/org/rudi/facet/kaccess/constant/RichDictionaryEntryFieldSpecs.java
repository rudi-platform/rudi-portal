package org.rudi.facet.kaccess.constant;

import org.rudi.facet.dataverse.fields.FieldSpec;

public class RichDictionaryEntryFieldSpecs {

	public final FieldSpec lang;
	public final FieldSpec text;
	public final FieldSpec html;

	private RichDictionaryEntryFieldSpecs(FieldSpec parentSpec) {
		lang = parentSpec.newChildFromJavaField(
						RichDictionaryEntryFieldSpecs.class,
						ConstantMetadata.LANG_FIELD_LOCAL_NAME
				).
				allowControlledVocabulary(false);
		text = parentSpec.newChildFromJavaField(
				RichDictionaryEntryFieldSpecs.class,
				ConstantMetadata.TEXT_FIELD_LOCAL_NAME
		);
		html = parentSpec.newChildFromJavaField(
				RichDictionaryEntryFieldSpecs.class,
				ConstantMetadata.HTML_FIELD_LOCAL_NAME
		);
	}

	public static RichDictionaryEntryFieldSpecs from(FieldSpec parentSpec) {
		return new RichDictionaryEntryFieldSpecs(parentSpec);
	}

	public FieldSpec[] toArray() {
		return new FieldSpec[] { lang, text, html };
	}
}
