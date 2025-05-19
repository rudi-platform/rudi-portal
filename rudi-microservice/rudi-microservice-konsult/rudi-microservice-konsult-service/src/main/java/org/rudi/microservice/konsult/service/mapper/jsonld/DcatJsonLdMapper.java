package org.rudi.microservice.konsult.service.mapper.jsonld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.kaccess.bean.Contact;
import org.rudi.facet.kaccess.bean.Licence;
import org.rudi.facet.kaccess.bean.LicenceStandard;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.facet.kaccess.bean.MediaService;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataList;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.jsonld.ContextHelper;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.impl.ContactPointJsonLdMapper;
import org.rudi.microservice.konsult.service.mapper.jsonld.impl.CreatorJsonLdMapper;
import org.rudi.microservice.konsult.service.mapper.jsonld.impl.DataServiceJsonLdMapper;
import org.rudi.microservice.konsult.service.mapper.jsonld.impl.DatasetJsonLdMapper;
import org.rudi.microservice.konsult.service.mapper.jsonld.impl.DistributionJsonLdMapper;
import org.rudi.microservice.konsult.service.mapper.jsonld.impl.PaginationJsonLdMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DcatJsonLdMapper extends AbstractJsonLdMapper<MetadataList> {

	private static final String NO_LICENSE = "notspecified";
	private static final String CUSTOM_LICENSE = "other-at";
	private static final String UNKNOWN_STANDARD_LICENSE = "other-pd";

	private final ContextHelper contextHelper;
	private final PaginationJsonLdMapper paginationJsonLdMapper;
	private final DatasetJsonLdMapper datasetJsonLdMapper;
	private final DistributionJsonLdMapper distributionJsonLdMapper;
	private final DataServiceJsonLdMapper dataServiceJsonLdMapper;
	private final ContactPointJsonLdMapper contactPointJsonLdMapper;
	private final CreatorJsonLdMapper creatorJsonLdMapper;

	@Value("#{${harvest.dcat.license:{'apache-2.0':'other-pd', 'cc-by-nd-4.0':'cc-by', 'etalab-1.0':'other-pd', 'etalab-2.0':'lov2', 'gpl-3.0':'other-pd', 'mit':'other-pd', 'odbl-1.0':'odc-odbl','public-domain-cc0':'other-pd'}}}")
	private Map<String, String> licenseMap;

	public DcatJsonLdMapper(MediaUrlHelper mediaUrlHelper, ContextHelper contextHelper,
			PaginationJsonLdMapper paginationJsonLdMapper, DatasetJsonLdMapper datasetJsonLdMapper,
			DistributionJsonLdMapper distributionJsonLdMapper, DataServiceJsonLdMapper dataServiceJsonLdMapper,
			ContactPointJsonLdMapper contactPointJsonLdMapper, CreatorJsonLdMapper creatorJsonLdMapper) {
		super(mediaUrlHelper);
		this.contextHelper = contextHelper;
		this.paginationJsonLdMapper = paginationJsonLdMapper;
		this.datasetJsonLdMapper = datasetJsonLdMapper;
		this.distributionJsonLdMapper = distributionJsonLdMapper;
		this.dataServiceJsonLdMapper = dataServiceJsonLdMapper;
		this.contactPointJsonLdMapper = contactPointJsonLdMapper;
		this.creatorJsonLdMapper = creatorJsonLdMapper;
	}

	@Override
	public JsonObject toJsonLd(MetadataList metadataList, DcatJsonLdContext context) throws AppServiceException {
		JsonObject result = new JsonObject();
		JsonArray graph = new JsonArray();

		graph.add(paginationJsonLdMapper.toJsonLd(context.getDatasetSearchCriteria(), context));
		context.setKinds(new ArrayList<>());
		if (metadataList != null && CollectionUtils.isNotEmpty(metadataList.getItems())) {

			metadataList.getItems().forEach(item -> {
				// initialisation des listes et des tableau pour ce dataset
				context.setContactPoints(new JsonArray());
				context.setDistributions(new JsonArray());
				context.setServices(new JsonArray());

				try {
					// Création du JSON de l'objet dataset
					JsonObject dataset = datasetJsonLdMapper.toJsonLd(item, context);

					// Récupération et maping de la licence du JDD
					extractLicense(item, context);

					// Récupération des contactPoints
					extractMetadataContacts(item, context);
					extractMetadataInfoContacts(item, context);

					// Récupération du producer
					extractProducer(item, context, dataset);

					// Gestion des médias de type service (dataservice), et dwnl (distributions)
					extractMedias(item, context, graph);

					if (!context.getContactPoints().isEmpty()) {
						dataset.add("contactPoint", context.getContactPoints());
					}

					if (!context.getDistributions().isEmpty()) {
						dataset.add("distribution", context.getDistributions());
					}

					if (!context.getServices().isEmpty()) {
						dataset.add("service", context.getServices());
					}

					graph.add(dataset);
				} catch (AppServiceException e) {
					log.error(e.getMessage());
				}
			});
		}

		context.getKinds().forEach(graph::add);

		result.add("@context", contextHelper.getContext());
		result.add("@graph", graph);
		return result;
	}

	private void extractMetadataContacts(Metadata metadata, DcatJsonLdContext context) {
		// Récupération des contacts
		metadata.getContacts().forEach(contact -> {
			try {
				addKind(context.getKinds(), contactPointJsonLdMapper.toJsonLd(contact, context));
				addContactPoint(context.getContactPoints(), contact.getContactId());
			} catch (AppServiceException e) {
				log.error(e.getMessage());
			}
		});
	}

	private void extractMetadataInfoContacts(Metadata metadata, DcatJsonLdContext context) {
		List<Contact> metadataContacts = metadata.getMetadataInfo().getMetadataContacts();
		if (CollectionUtils.isNotEmpty(metadataContacts)) {
			metadataContacts.forEach(metadataContact -> {
				try {
					addKind(context.getKinds(), contactPointJsonLdMapper.toJsonLd(metadataContact, context));
					addContactPoint(context.getContactPoints(), metadataContact.getContactId());
				} catch (AppServiceException e) {
					log.error(e.getMessage());
				}
			});
		}
	}

	private void extractProducer(Metadata metadata, DcatJsonLdContext context, JsonObject dataset)
			throws AppServiceException {
		// Gestion du producer
		JsonObject creator = creatorJsonLdMapper.toJsonLd(metadata.getProducer(), context);
		if (creator != null && !creator.isEmpty()) {
			addKind(context.getKinds(), creator);
			JsonObject dctCreator = new JsonObject();
			dctCreator.addProperty("@id", metadata.getProducer().getOrganizationId().toString());
			dataset.add("dct:creator", dctCreator);
		}
	}

	private void extractMedias(Metadata metadata, DcatJsonLdContext context, JsonArray graph) {
		metadata.getAvailableFormats().forEach(media -> {
			try {
				if (Media.MediaTypeEnum.FILE.equals(media.getMediaType())) {
					JsonObject distribution = distributionJsonLdMapper.toJsonLd((MediaFile) media, context);

					// Rajout du media au @graph
					graph.add(distribution);

					// Liste des fichiers du JDD en cours de transcription
					context.getDistributions().add(media.getMediaId().toString());
				} else if (Media.MediaTypeEnum.SERVICE.equals(media.getMediaType())) {
					JsonObject dataService = dataServiceJsonLdMapper.toJsonLd((MediaService) media, context);

					// Rajout du service au @graph
					graph.add(dataService);

					// Liste des services du JDD en cours de transcription
					context.getServices().add(media.getMediaId().toString());
				}
				// On ne traite pas les MediaType SERIES
			} catch (AppServiceException e) {
				log.error(e.getMessage());
			}
		});
	}

	private void extractLicense(Metadata metadata, DcatJsonLdContext context) {
		Licence licence = metadata.getAccessCondition().getLicence();
		JsonObject license = new JsonObject();
		if (licence != null) {
			if (Licence.LicenceTypeEnum.STANDARD.equals(licence.getLicenceType())) {
				String key = ((LicenceStandard) licence).getLicenceLabel().getValue();
				license.addProperty("id", licenseMap.getOrDefault(key, UNKNOWN_STANDARD_LICENSE));
			} else {
				license.addProperty("id", CUSTOM_LICENSE);
			}
		} else {
			license.addProperty("id", NO_LICENSE);
		}
		context.setLicense(license);
	}

	/**
	 * Évite les doublons dans la liste des organizations et complète si une des entités à plus d'informations.
	 *
	 * @param kinds la list des dct:Kind
	 * @param kind  le dct:Kind que l'on souhaite ajouter
	 */
	private void addKind(List<JsonObject> kinds, JsonObject kind) {
		kinds.stream().filter(k -> k.asMap().containsValue(kind.asMap().get("@id"))).findFirst().ifPresentOrElse(k -> {
			Map<String, JsonElement> existingMap = k.asMap();
			Map<String, JsonElement> newMap = kind.asMap();
			newMap.forEach((key, value) -> {
				if (!existingMap.containsKey(key)) {
					existingMap.put(key, value);
				}
			});
		}, () -> kinds.add(kind));

	}

	/**
	 * Évite les doublons dans la liste des contacts
	 *
	 * @param contactPoints la liste des contacts
	 * @param contactUuid   l'identifiant du contact à ajouter à la liste
	 */
	private void addContactPoint(JsonArray contactPoints, UUID contactUuid) {
		Optional<JsonElement> existingElement = contactPoints.asList().stream()
				.filter(e -> contactUuid.toString().equals(e.getAsJsonObject().get("@id").getAsString())).findFirst();

		if (existingElement.isEmpty()) {
			JsonObject contactPoint = new JsonObject();
			contactPoint.addProperty("@id", contactUuid.toString());

			contactPoints.add(contactPoint);
		}
	}

}
