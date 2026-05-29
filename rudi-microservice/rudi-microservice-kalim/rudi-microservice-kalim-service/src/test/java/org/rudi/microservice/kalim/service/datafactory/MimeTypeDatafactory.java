package org.rudi.microservice.kalim.service.datafactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.rudi.common.service.datafactory.AbstractStampedDataFactory;
import org.rudi.microservice.kalim.storage.dao.mimetype.MimeTypeDao;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class MimeTypeDatafactory extends AbstractStampedDataFactory<MimeTypeEntity, MimeTypeDao> {

	private static final int DEFAULT_ORDER = 10;
	private static final String CSV_CODE = "text/csv";
	private static final String CSV_LABEL = "CSV";
	private static final String PDF_CODE = "application/pdf";
	private static final String PDF_LABEL = "PDF";
	private static final String JSON_CODE = "application/json";
	private static final String JSON_LABEL = "JSON";
	private static final String XML_CODE = "application/xml";
	private static final String XML_LABEL = "XML";
	private static final String MARKDOWN_CODE = "text/markdown";
	private static final String MARKDOWN_LABEL = "Markdown";
	private static final String ZIP_CODE = "application/zip";
	private static final String ZIP_LABEL = "ZIP";
	private static final String IT_ACTIVE_PASS_1_CODE = "application/x-rudi-it-pass-1";
	private static final String IT_ACTIVE_PASS_2_CODE = "application/x-rudi-it-pass-2";
	private static final String IT_INACTIVE_FAIL_1_CODE = "application/x-rudi-it-fail-1";
	private static final String IT_INACTIVE_FAIL_2_CODE = "application/x-rudi-it-fail-2";

	public MimeTypeDatafactory(MimeTypeDao repository) {
		super(repository, MimeTypeEntity.class);
	}

	public MimeTypeEntity getOrCreateActiveCSVMimeType() {
		return getOrCreate(CSV_CODE, CSV_LABEL, DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(1), null);
	}

	public MimeTypeEntity getOrCreateActivePDFMimeType() {
		return getOrCreate(PDF_CODE, PDF_LABEL, DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(1), null);
	}

	public MimeTypeEntity getOrCreateActiveJSONMimeType() {
		return getOrCreate(JSON_CODE, JSON_LABEL, DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(1), null);
	}

	public MimeTypeEntity getOrCreateInactiveXMLMimeType() {
		return getOrCreate(XML_CODE, XML_LABEL, DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(2), LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
	}

	public MimeTypeEntity getOrCreateInactiveMarkdownMimeType() {
		return getOrCreate(MARKDOWN_CODE, MARKDOWN_LABEL, DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(2), LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
	}

	public MimeTypeEntity getOrCreateInactiveZipMimeType() {
		return getOrCreate(ZIP_CODE, ZIP_LABEL, DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(2), LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
	}

	public MimeTypeEntity getOrCreateValidatorItActivePassMimeType1() {
		return getOrCreate(IT_ACTIVE_PASS_1_CODE, "Validator IT pass 1", DEFAULT_ORDER,
				LocalDateTime.now(ZoneOffset.UTC).minusDays(1), null);
	}

	public MimeTypeEntity getOrCreateValidatorItActivePassMimeType2() {
		return getOrCreate(IT_ACTIVE_PASS_2_CODE, "Validator IT pass 2", DEFAULT_ORDER,
				LocalDateTime.now(ZoneOffset.UTC).minusDays(1), null);
	}

	public MimeTypeEntity getOrCreateValidatorItActiveMixedMimeType() {
		final String uniqueCode = "application/x-it-ma-" + UUID.randomUUID();
		return getOrCreate(uniqueCode, "Validator IT mixed active", DEFAULT_ORDER,
				LocalDateTime.now(ZoneOffset.UTC).minusDays(1), null);
	}

	public MimeTypeEntity getOrCreateValidatorItInactiveMixedMimeType() {
		final String uniqueCode = "application/x-it-mi-" + UUID.randomUUID();
		return getOrCreate(uniqueCode, "Validator IT mixed inactive", DEFAULT_ORDER,
				LocalDateTime.now(ZoneOffset.UTC).minusDays(2), LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
	}

	public MimeTypeEntity getOrCreateValidatorItInactiveFailMimeType1() {
		return getOrCreate(IT_INACTIVE_FAIL_1_CODE, "Validator IT fail 1", DEFAULT_ORDER,
				LocalDateTime.now(ZoneOffset.UTC).minusDays(2), LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
	}

	public MimeTypeEntity getOrCreateValidatorItInactiveFailMimeType2() {
		return getOrCreate(IT_INACTIVE_FAIL_2_CODE, "Validator IT fail 2", DEFAULT_ORDER,
				LocalDateTime.now(ZoneOffset.UTC).minusDays(2), LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
	}

	public MimeTypeEntity getOrCreateActiveUniqueMimeType() {
		final String uniqueCode = "application/x-ut-active-" + UUID.randomUUID();
		return getOrCreate(uniqueCode, "UT active", DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(1), null);
	}

	public MimeTypeEntity getOrCreateInactiveUniqueMimeType() {
		final String uniqueCode = "application/x-ut-inactive-" + UUID.randomUUID();
		return getOrCreate(uniqueCode, "UT inactive", DEFAULT_ORDER, LocalDateTime.now(ZoneOffset.UTC).minusDays(2), LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
	}
}
