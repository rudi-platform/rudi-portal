package org.rudi.microservice.projekt.service.datafactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectStatus;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelatedOrganizationEntity;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DataFactory pour créer/récupérer des projets dans les tests.
 *
 * Les méthodes getOrCreate[NomDuProjet] permettent de créer un projet s'il n'existe pas en BDD, ou de le récupérer sinon (idempotent).
 *
 * UUIDs constants pour chaque type de projet permettent l'idempotence.
 *
 * Chaque méthode peut être configurée pour inclure : - linkedDatasets - newDatasetRequests - relatedOrganizations
 */
@Component
@Transactional
public class ProjectDataFactory extends AbstractAssetDescriptionDataFactory<ProjectEntity, ProjectDao> {

	// UUIDs constants pour chaque type de projet (permet idempotence)
	private static final UUID LAMPADAIRES_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID POUBELLES_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID PARK_BIKE_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final ConfidentialityDataFactory confidentialityDataFactory;
	private final ReutilisationStatusDataFactory reutilisationStatusDataFactory;
	private final LinkedDatasetDataFactory linkedDatasetDataFactory;
	private final NewDatasetRequestDataFactory newDatasetRequestDataFactory;

	public ProjectDataFactory(ProjectDao repository, ConfidentialityDataFactory confidentialityDataFactory,
			ReutilisationStatusDataFactory reutilisationStatusDataFactory,
			LinkedDatasetDataFactory linkedDatasetDataFactory,
			NewDatasetRequestDataFactory newDatasetRequestDataFactory) {
		super(repository, ProjectEntity.class);
		this.confidentialityDataFactory = confidentialityDataFactory;
		this.reutilisationStatusDataFactory = reutilisationStatusDataFactory;
		this.linkedDatasetDataFactory = linkedDatasetDataFactory;
		this.newDatasetRequestDataFactory = newDatasetRequestDataFactory;
	}

	/**
	 * Crée un projet basique
	 */
	private ProjectEntity createBaseProject(UUID projectUuid, String title, String description, UUID ownerUuid,
			OwnerType ownerType) {
		ProjectEntity project = new ProjectEntity();
		project.setUuid(projectUuid);
		project.setTitle(title);
		project.setDescription(description);
		project.setOwnerUuid(ownerUuid);
		project.setOwnerType(ownerType);
		project.setContactEmail(randomString(10) + "@test.fr");
		project.setStatus(Status.COMPLETED);
		project.setProjectStatus(ProjectStatus.DRAFT);
		project.setFunctionalStatus("Créé");
		project.setInitiator("datafactory");
		project.setProcessDefinitionKey("project-process");
		LocalDateTime now = LocalDateTime.now();
		project.setCreationDate(now);
		project.setUpdatedDate(now);
		project.setExpectedCompletionStartDate(now);
		project.setExpectedCompletionEndDate(now.plusMonths(6));
		project.setConfidentiality(confidentialityDataFactory.getOrCreatePublicConfidentiality());
		project.setReutilisationStatus(reutilisationStatusDataFactory.getOrCreateProjectStatus());
		project.setThemes(new HashSet<>());
		project.setKeywords(new HashSet<>());
		project.setTargetAudiences(new HashSet<>());
		project.setDesiredSupports(new HashSet<>());
		project.setLinkedDatasets(new HashSet<>());
		project.setDatasetRequests(new HashSet<>());
		project.setRelatedOrganizations(new HashSet<>());
		return project;
	}

	/**
	 * Ajoute un LinkedDataset au projet
	 */
	private void addLinkedDataset(ProjectEntity project, UUID datasetUuid, UUID organizationUuid) {
		project.getLinkedDatasets().add(linkedDatasetDataFactory.createNotPersisted(datasetUuid, organizationUuid,
				LinkedDatasetStatus.DRAFT, DatasetConfidentiality.OPENED));
	}

	/**
	 * Ajoute une demande de NewDatasetRequest au projet
	 */
	private void addNewDatasetRequest(ProjectEntity project, String title) {
		project.getDatasetRequests()
				.add(newDatasetRequestDataFactory.createNotPersisted(title, NewDatasetRequestStatus.DRAFT));
	}

	/**
	 * Ajoute une organization liée au projet
	 */
	private void addRelatedOrganization(ProjectEntity project, UUID organizationUuid, RelationStatus relationStatus) {
		if (organizationUuid != null && relationStatus != null) {

			project.getRelatedOrganizations()
					.add(createNotPersistedRelatedOrganizationEntity(organizationUuid, relationStatus));
		}
	}

	private UUID resolveProjectUuid(UUID projectUuid) {
		return projectUuid != null ? projectUuid : UUID.randomUUID();
	}

	/**
	 * Crée ou récupère un projet "Lampadaires" avec LinkedDatasets et NewDatasetRequest
	 */
	public ProjectEntity getOrCreateLampadairesWithLinkedAndNew(UUID ownerUuid, OwnerType ownerType) {
		ProjectEntity existingProject = repository.findByUuid(LAMPADAIRES_UUID);
		if (existingProject != null) {
			return existingProject;
		}

		return repository.save(getLampadairesWithLinkedAndNew(ownerUuid, ownerType, LAMPADAIRES_UUID));
	}

	/**
	 * Retourne un projet non persisté "Lampadaires" avec LinkedDatasets et NewDatasetRequest
	 */
	public ProjectEntity getLampadairesWithLinkedAndNew(UUID ownerUuid, OwnerType ownerType, UUID projectUuid) {
		ProjectEntity project = createBaseProject(resolveProjectUuid(projectUuid), "Lampadaires - Full",
				"Projet de comptage des lampadaires avec données", ownerUuid, ownerType);

		// Ajoute 2 LinkedDatasets
		addLinkedDataset(project, UUID.randomUUID(), UUID.randomUUID());
		addLinkedDataset(project, UUID.randomUUID(), UUID.randomUUID());

		// Ajoute 1 NewDatasetRequest
		addNewDatasetRequest(project, "Demande de données supplémentaires pour lampadaires");

		return project;
	}

	/**
	 * Crée ou récupère un projet "Poubelles" dont le propriétaire est obligatoirement une organisation. Le projet contient : - 1 LinkedDataset
	 * (obligatoire selon les règles métier) - le ownerUuid dans les RelatedOrganizations - les autres organisations passées en paramètre dans les
	 * RelatedOrganizations
	 *
	 * Ce cas est volontairement invalide : le propriétaire (organisation) est aussi une RelatedOrganization, ce qui doit produire une erreur dans les
	 * TUs.
	 */
	public ProjectEntity getOrCreatePoubelleWithRelatedOrganizations(UUID ownerUuid, Set<UUID> organizationUuids) {
		ProjectEntity existingProject = repository.findByUuid(POUBELLES_UUID);
		if (existingProject != null) {
			return existingProject;
		}

		return repository.save(getPoubelleWithRelatedOrganizations(ownerUuid, organizationUuids, POUBELLES_UUID));
	}

	/**
	 * Retourne un projet non persisté "Poubelles" (cas invalide intentionnel)
	 */
	public ProjectEntity getPoubelleWithRelatedOrganizations(UUID ownerUuid, Set<UUID> organizationUuids,
			UUID projectUuid) {
		ProjectEntity project = createBaseProject(resolveProjectUuid(projectUuid), "Poubelles - With Orga",
				"Projet de suivi des poubelles avec organisations liées", ownerUuid, OwnerType.ORGANIZATION);

		// Ajoute 1 LinkedDataset (obligatoire)
		addLinkedDataset(project, UUID.randomUUID(), UUID.randomUUID());

		// Ajoute le propriétaire dans les RelatedOrganizations (cas invalide intentionnel)
		addRelatedOrganization(project, ownerUuid, RelationStatus.ACCEPTED);

		// Ajoute les autres organisations liées
		if (organizationUuids != null) {
			organizationUuids.forEach(orgUuid -> addRelatedOrganization(project, orgUuid, RelationStatus.ACCEPTED));
		}

		return project;
	}

	/**
	 * Crée ou récupère un projet "Park Bike" avec LinkedDatasets et RelatedOrganizations
	 */
	public ProjectEntity getOrCreateParkBikeWithLinkedAndOrganizations(UUID ownerUuid, OwnerType ownerType,
			Set<UUID> organizationUuids) {
		ProjectEntity existingProject = repository.findByUuid(PARK_BIKE_UUID);
		if (existingProject != null) {
			return existingProject;
		}

		return repository
				.save(getParkBikeWithLinkedAndOrganizations(ownerUuid, ownerType, organizationUuids, PARK_BIKE_UUID));
	}

	/**
	 * Retourne un projet non persisté "Park Bike" avec LinkedDatasets et RelatedOrganizations
	 */
	public ProjectEntity getParkBikeWithLinkedAndOrganizations(UUID ownerUuid, OwnerType ownerType,
			Set<UUID> organizationUuids, UUID projectUuid) {
		ProjectEntity project = createBaseProject(resolveProjectUuid(projectUuid), "Park Bike - Full",
				"Projet de localisation des parcs à vélos avec organisations partenaires", ownerUuid, ownerType);

		// Ajoute 1 LinkedDataset
		addLinkedDataset(project, UUID.randomUUID(), UUID.randomUUID());

		// Ajoute les organisations liées
		if (organizationUuids != null) {
			organizationUuids.forEach(orgUuid -> addRelatedOrganization(project, orgUuid, RelationStatus.PENDING));
		}

		return project;
	}

	/**
	 * Génère un UUID déterministe basé sur le nom et le type de propriétaire (pour idempotence)
	 */
	private UUID generateDeterministicUuid(String baseName, OwnerType ownerType) {
		return UUID.nameUUIDFromBytes((baseName + "-" + ownerType.toString()).getBytes());
	}

	/**
	 * Crée ou récupère un projet minimal (sans dépendances)
	 */
	public ProjectEntity getOrCreateSimpleProject(String name, UUID ownerUuid, OwnerType ownerType) {
		UUID projectUuid = generateDeterministicUuid(name, ownerType);
		ProjectEntity existingProject = repository.findByUuid(projectUuid);
		if (existingProject != null) {
			return existingProject;
		}

		ProjectEntity project = createBaseProject(projectUuid, name, "Projet simple " + name, ownerUuid, ownerType);

		return repository.save(project);
	}

	/**
	 * Crée ou récupère un projet avec uniquement des LinkedDatasets
	 */
	public ProjectEntity getOrCreateProjectWithLinkedDatasetsOnly(String name, UUID ownerUuid, OwnerType ownerType,
			int numberOfDatasets) {
		UUID projectUuid = generateDeterministicUuid(name + "-LinkedDatasets-" + numberOfDatasets, ownerType);
		ProjectEntity existingProject = repository.findByUuid(projectUuid);
		if (existingProject != null) {
			return existingProject;
		}

		ProjectEntity project = createBaseProject(projectUuid, name, "Projet avec datasets liés", ownerUuid, ownerType);

		// Ajoute N LinkedDatasets
		for (int i = 0; i < numberOfDatasets; i++) {
			addLinkedDataset(project, UUID.randomUUID(), UUID.randomUUID());
		}

		return repository.save(project);
	}

	/**
	 * Crée ou récupère un projet avec uniquement des NewDatasetRequests
	 */
	public ProjectEntity getOrCreateProjectWithNewDatasetRequestsOnly(String name, UUID ownerUuid, OwnerType ownerType,
			int numberOfRequests) {
		UUID projectUuid = generateDeterministicUuid(name + "-NewDatasetRequests-" + numberOfRequests, ownerType);
		ProjectEntity existingProject = repository.findByUuid(projectUuid);
		if (existingProject != null) {
			return existingProject;
		}

		ProjectEntity project = createBaseProject(projectUuid, name, "Projet avec demandes de données", ownerUuid,
				ownerType);

		// Ajoute N NewDatasetRequests
		for (int i = 0; i < numberOfRequests; i++) {
			addNewDatasetRequest(project, "Demande n°" + (i + 1));
		}

		return repository.save(project);
	}

	/**
	 * Crée ou récupère un projet avec uniquement des OrganizationsRelated
	 * 
	 * @param accepted
	 */
	public ProjectEntity getOrCreateProjectWithRelatedOrganizationsOnly(String name, ProjectStatus projectStatus,
			UUID ownerUuid, OwnerType ownerType, Set<UUID> organizationUuids, RelationStatus relationStatus) {
		String orgUuidsString = organizationUuids != null
				? organizationUuids.stream().map(UUID::toString).sorted().reduce("", String::concat)
				: "empty";
		UUID projectUuid = generateDeterministicUuid(name + "-RelatedOrganizations-" + orgUuidsString, ownerType);
		ProjectEntity existingProject = repository.findByUuid(projectUuid);
		if (existingProject != null) {
			return existingProject;
		}

		ProjectEntity project = createBaseProject(projectUuid, name, "Projet avec organisations liées", ownerUuid,
				ownerType);
		project.setProjectStatus(projectStatus != null ? projectStatus : ProjectStatus.DRAFT);

		// Ajoute les organisations liées
		if (organizationUuids != null) {
			organizationUuids.forEach(orgUuid -> addRelatedOrganization(project, orgUuid,
					relationStatus != null ? relationStatus : RelationStatus.PENDING));
		}

		return repository.save(project);
	}

	/**
	 * Crée ou récupère un projet complet (LinkedDatasets + NewDatasetRequests + RelatedOrganizations)
	 */
	public ProjectEntity getOrCreateCompleteProject(String name, UUID ownerUuid, OwnerType ownerType,
			Set<UUID> organizationUuids) {
		String orgUuidsString = organizationUuids != null
				? organizationUuids.stream().map(UUID::toString).sorted().reduce("", String::concat)
				: "empty";
		UUID projectUuid = generateDeterministicUuid(name + "-Complete-" + orgUuidsString, ownerType);
		ProjectEntity existingProject = repository.findByUuid(projectUuid);
		if (existingProject != null) {
			return existingProject;
		}

		ProjectEntity project = createBaseProject(projectUuid, name, "Projet complet avec tous les éléments", ownerUuid,
				ownerType);

		// Ajoute 2 LinkedDatasets
		addLinkedDataset(project, UUID.randomUUID(), UUID.randomUUID());
		addLinkedDataset(project, UUID.randomUUID(), UUID.randomUUID());

		// Ajoute 2 NewDatasetRequests
		addNewDatasetRequest(project, "Demande 1");
		addNewDatasetRequest(project, "Demande 2");

		// Ajoute les organisations liées
		if (organizationUuids != null) {
			organizationUuids.forEach(orgUuid -> addRelatedOrganization(project, orgUuid, RelationStatus.ACCEPTED));
		}

		return repository.save(project);
	}

	public RelatedOrganizationEntity createNotPersistedRelatedOrganizationEntity(UUID organizationUuid,
			RelationStatus relationStatus) {
		RelatedOrganizationEntity relatedOrganization = new RelatedOrganizationEntity();
		relatedOrganization.setUuid(UUID.randomUUID());
		relatedOrganization.setOrganizationUuid(organizationUuid != null ? organizationUuid : UUID.randomUUID());
		relatedOrganization.setRelationStatus(relationStatus != null ? relationStatus : RelationStatus.PENDING);
		return relatedOrganization;
	}
}
