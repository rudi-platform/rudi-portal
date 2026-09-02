package org.rudi.facet.kaccess.datafactory;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.Organization;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets Organization.
 * Une Organization représente une organisation (producteur, fournisseur de métadonnées, etc.)
 */
@Component
@RequiredArgsConstructor
public class OrganizationDataFactory {

	/**
	 * Crée une organisation standard (producteur RUDI).
	 * IMPORTANT : L'organization_id est fixé pour la réentrance des tests.
	 * // Aucun get possible
	 */
	public Organization createOrganizationRudiProducer() {
		return createOrganization(UUID.fromString("acdccf43-566b-4134-b39e-ddf46c801242"), "Producteur rudi",
				"2 rue de rudi");
	}

	/**
	 * Crée une organisation fournisseur de métadonnées.
	 * IMPORTANT : L'organization_id est fixé pour la réentrance des tests.
	 * // Aucun get possible
	 */
	public Organization createOrganizationMetadataProvider() {
		return createOrganization(UUID.fromString("1b988e5d-3c5d-4bcb-84f9-8a6702d5d7c0"), "Fournisseur rudi",
				"4 rue de la pomme");
	}

	/**
	 * Crée une organisation minimale (propriétés obligatoires uniquement).
	 * IMPORTANT : L'organization_id est fixé pour la réentrance des tests.
	 * // Aucun get possible
	 */
	public Organization createOrganizationMinimal() {
		return createOrganization(UUID.fromString("df2148ae-4e33-4a34-8a9e-c2e04db10ebd"), "STAR", null);
	}

	/**
	 * Crée une organisation Keolis (producteur spécifique de test).
	 * IMPORTANT : L'organization_id est généré aléatoirement car aucun UUID existant n'est fourni.
	 * // Aucun get possible
	 */
	public Organization createOrganizationKeolis() {
		return createOrganization(UUID.randomUUID(), "Keolis", null);
	}

	private Organization createOrganization(UUID id, String name, String address) {
		Organization org = new Organization();
		org.setOrganizationId(id);
		org.setOrganizationName(name);
		org.setOrganizationAddress(address);
		return org;
	}

}

