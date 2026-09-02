package org.rudi.facet.kaccess.datafactory;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.Contact;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets Contact.
 * Un Contact représente une personne ou organisation de contact pour un dataset.
 */
@Component
@RequiredArgsConstructor
public class ContactDataFactory {

	private static final UUID CONTACT_ID_PIERRE_DUPONT = UUID.fromString("2fdf4ec4-4178-4d83-86ff-96ecdf9f2a85");
	private static final UUID CONTACT_ID_RUDI = UUID.fromString("96f65708-6593-4694-8fdb-7f4f3f96bd4b");
	private static final UUID CONTACT_ID_MINIMAL = UUID.fromString("8d2fe594-c070-436c-8be9-3fd0e2a9da31");

	private Contact createContact(UUID contactId, String contactName, String organizationName, String role, String email) {
		Contact contact = new Contact();
		contact.setContactId(contactId);
		contact.setContactName(contactName);
		contact.setOrganizationName(organizationName);
		contact.setRole(role);
		contact.setEmail(email);
		return contact;
	}

	/**
	 * Crée un contact standard pour les métadonnées.
	 * Le contact_id est fixe pour garantir la stabilité des fixtures de référence.
	 */
	public Contact createContactPierreDupont() {
		return createContact(CONTACT_ID_PIERRE_DUPONT, "Dupont Pierre", "Association Nature", "manager", "dupont@onu.org");
	}

	/**
	 * Crée un contact d'équipe RUDI.
	 * Le contact_id est fixe pour garantir la stabilité des fixtures de référence.
	 */
	public Contact createContactRudi() {
		return createContact(CONTACT_ID_RUDI, "rudi contact for test", "rudi corp", "admin", "rudi@onu.org");
	}

	/**
	 * Crée un contact minimal (propriétés obligatoires uniquement).
	 * Le contact_id est fixe pour garantir la stabilité des fixtures de référence.
	 */
	public Contact createContactMinimal() {
		return createContact(CONTACT_ID_MINIMAL, "Nom du contact", null, null, "contact_test@rudi.fr");
	}

}

