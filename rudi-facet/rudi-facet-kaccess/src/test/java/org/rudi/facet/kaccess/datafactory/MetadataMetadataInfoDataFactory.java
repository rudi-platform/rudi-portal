package org.rudi.facet.kaccess.datafactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.rudi.facet.kaccess.bean.Contact;
import org.rudi.facet.kaccess.bean.MetadataMetadataInfo;
import org.rudi.facet.kaccess.bean.ReferenceDates;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer des objets MetadataMetadataInfo.
 * Gère le provider, les contacts et les dates de métadonnées.
 */
@Component
@RequiredArgsConstructor
public class MetadataMetadataInfoDataFactory {

	private final OrganizationDataFactory organizationDataFactory;

	private static final String METADATA_API_VERSION = "1.2.0";

	/**
	 * Crée les métadonnées standard (fixture agri.json).
	 */
	public MetadataMetadataInfo createMetadataMetadataInfoStandard() {
		MetadataMetadataInfo metadataInfo = new MetadataMetadataInfo();
		metadataInfo.setApiVersion(METADATA_API_VERSION);
		metadataInfo.setMetadataDates(createStandardMetadataDates());
		metadataInfo.setMetadataProvider(organizationDataFactory.createOrganizationMetadataProvider());
		metadataInfo.setMetadataContacts(createStandardMetadataContacts());
		return metadataInfo;
	}

	/**
	 * Crée les métadonnées minimales.
	 */
	public MetadataMetadataInfo createMetadataMetadataInfoMinimal() {
		MetadataMetadataInfo metadataInfo = new MetadataMetadataInfo();
		metadataInfo.setApiVersion(METADATA_API_VERSION);
		return metadataInfo;
	}

	private ReferenceDates createStandardMetadataDates() {
		ReferenceDates referenceDates = new ReferenceDates();
		referenceDates.setCreated(parseDate("2021-03-01T00:00:00Z"));
		referenceDates.setValidated(parseDate("2021-03-02T00:00:00Z"));
		referenceDates.setPublished(parseDate("2021-03-03T00:00:00Z"));
		referenceDates.setUpdated(parseDate("2021-03-11T07:00:00Z"));
		referenceDates.setDeleted(parseDate("2021-03-22T00:00:00Z"));
		return referenceDates;
	}

	private OffsetDateTime parseDate(String date) {
		return OffsetDateTime.parse(date);
	}

	private List<Contact> createStandardMetadataContacts() {
		return List.of(
				createContact("3b031485-5a9b-436c-abcc-053710f17994", "Contact metadata 1",
						"organisation metadata 1", "communication 1", "rudiMetadata@onu.org"),
				createContact("c9475458-ccdd-4db5-9be7-96ee2ce46528", "Contact metadata 2",
						"organisation metadata 2", "communication 2", "contactMetadata@asso.org"));
	}

	private Contact createContact(String id, String name, String organizationName, String role, String email) {
		Contact contact = new Contact();
		contact.setContactId(UUID.fromString(id));
		contact.setContactName(name);
		contact.setOrganizationName(organizationName);
		contact.setRole(role);
		contact.setEmail(email);
		return contact;
	}

}

