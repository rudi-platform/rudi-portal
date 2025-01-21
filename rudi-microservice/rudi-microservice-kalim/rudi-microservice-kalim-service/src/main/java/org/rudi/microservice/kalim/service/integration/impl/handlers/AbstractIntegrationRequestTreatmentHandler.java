package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.apigateway.exceptions.ApiGatewayApiException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.providers.bean.LinkedProducer;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.core.bean.IntegrationStatus;
import org.rudi.microservice.kalim.core.exception.IntegrationException;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractIntegrationRequestTreatmentHandler {

	protected final DatasetService datasetService;
	protected final ApiManagerHelper apiGatewayManagerHelper;
	private final Error500Builder error500Builder;
	protected final ProviderHelper providerHelper;
	protected final ObjectMapper objectMapper;
	protected final ACLHelper aclHelper;
	protected final RolesHelper roleHelper;

	public void handle(IntegrationRequestEntity integrationRequest) {
		try {
			handleInternal(integrationRequest);
		} catch (Exception e) {
			integrationRequest.setIntegrationStatus(IntegrationStatus.KO);
			integrationRequest.setErrors(Collections.singleton(error500Builder.build()));
			log.info("Handle Integration request treatment KO.", e);
		}
	}

	/**
	 * Vérifie que le NodeProvider existe
	 *
	 * @param nodeProviderId un UUID
	 * @return un booleen indiquant si NodeProvider existe
	 */
	public boolean isUserNodeProvider(UUID nodeProviderId) {
		if (nodeProviderId == null) {
			return false;
		}

		return providerHelper.getNodeProviderByUUID(nodeProviderId) != null;
	}

	/**
	 * Vérifie que le provider dans les metadatas est le même que le provider
	 *
	 * @param metadata des Metadata
	 * @param provider un Provider
	 * @return un booleen indiquant si le provider dans les metadatas est le même que le provider
	 * Renvoie true si le provider dans les metadatas est null
	 */
	public boolean isSameProviderOrNull(Metadata metadata, Provider provider) {
		// Si la valeur du provider n'est pas renseigné alors on authorise
		if (metadata.getMetadataInfo().getMetadataProvider() == null || metadata.getMetadataInfo().getMetadataProvider().getOrganizationId() == null) {
			return true;
		}
		// on vérifie que le Provider correspond à l'utilisateur actuel
		return metadata.getMetadataInfo().getMetadataProvider().getOrganizationId().equals(provider.getUuid());
	}

	/**
	 * Vérifie l'existence d'un lien (linked-producer) entre un provider et une organisation
	 *
	 * @param metadata des Metadata
	 * @param provider un Provider
	 * @return un booleen indiquant la présence du lien (vrai si existence d'un lien)
	 */
	public boolean isLinkedToOrganization(Metadata metadata, Provider provider) {
		Optional<LinkedProducer> optLinkedProducer = provider.getLinkedProducers().stream()
				.filter(lp -> lp.getOrganization().getUuid().equals(metadata.getProducer().getOrganizationId()))
				.findFirst();

		return optLinkedProducer.isPresent();
	}

	/**
	 * Transforme une chaine de caractères en Metadata.
	 *
	 * @param file la chaine de caractères qui contient un metadata au format JSON
	 * @return Metadata l'objet java Metadata obtenu à partir du contenu JSON désérialisé
	 * @throws IntegrationException en cas d'erreur lors du parsing JSON de la metadata
	 */
	public Metadata hydrateMetadata(String file) throws IntegrationException {
		try {
			return objectMapper.readValue(file, Metadata.class);
		} catch (Exception e) {
			throw new IntegrationException(
					"Error lors de la récupération des Metadata dans l'Integration Request : transformation JSON -> Metadata",
					e);
		}
	}

	protected abstract void handleInternal(IntegrationRequestEntity integrationRequest)
			throws IntegrationException, DataverseAPIException, ApiGatewayApiException;

}
