package org.rudi.facet.kaccess.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantMetadata {

	public static final String DOI_REGEX = "^doi:10.\\d{4,9}/[-.;()/:\\w]+$";

	public static final String CURRENT_METADATA_VERSION = "1.4.1";
	public static final String LANG_FIELD_LOCAL_NAME = "lang";
	public static final String LANG_FIELD_SUFFIX = "_" + LANG_FIELD_LOCAL_NAME;
	public static final String TEXT_FIELD_LOCAL_NAME = "text";
	public static final String TEXT_FIELD_SUFFIX = "_" + TEXT_FIELD_LOCAL_NAME;
	public static final String HTML_FIELD_LOCAL_NAME = "html";

	@SuppressWarnings("squid:S2386") // Utilisé par org.rudi.microservice.kalim.service.helper.ValidationHelper
	public static final String[] SUPPORTED_METADATA_VERSIONS = {
			"1.2.0",
			"1.2.1",
			"1.3.0",
			"1.4.0",
			CURRENT_METADATA_VERSION };

}
