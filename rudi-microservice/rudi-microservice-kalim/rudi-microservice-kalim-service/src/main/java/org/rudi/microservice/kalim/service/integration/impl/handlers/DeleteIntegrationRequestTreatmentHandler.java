package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rudi.common.core.security.Role;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.apigateway.exceptions.DeleteApiException;
import org.rudi.facet.dataverse.api.exceptions.DatasetNotFoundException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.helper.dataset.metadatadetails.MetadataDetailsHelper;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.core.bean.IntegrationStatus;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.EmailHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.Project;
import org.rudi.microservice.projekt.core.bean.ProjectSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_108;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_109;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_110;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_111;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_112;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_500;

@Component
@Slf4j
public class DeleteIntegrationRequestTreatmentHandler extends AbstractIntegrationRequestTreatmentHandler {
	protected final MetadataDetailsHelper metadataDetailsHelper;
	protected final ProjektHelper projectHelper;
	protected final UtilPageable utilPageable;
	protected final EmailHelper emailHelper;

	protected DeleteIntegrationRequestTreatmentHandler(DatasetService datasetService,
			ApiManagerHelper apigatewayManagerHelper,
			Error500Builder error500Builder,
			MetadataDetailsHelper metadataDetailsHelper, ACLHelper aclHelper, ProviderHelper providerHelper, ObjectMapper objectMapper, RolesHelper roleHelper, ProjektHelper projectHelper, UtilPageable utilPageable, EmailHelper emailHelper) {
		super(datasetService, apigatewayManagerHelper, error500Builder, providerHelper, objectMapper, aclHelper, roleHelper);
		this.metadataDetailsHelper = metadataDetailsHelper;
		this.projectHelper = projectHelper;
		this.utilPageable = utilPageable;
		this.emailHelper = emailHelper;
	}

	@Override
	protected void handleInternal(IntegrationRequestEntity integrationRequest)
			throws DataverseAPIException, DeleteApiException {
		if (validateAndSetErrors(integrationRequest)) {
			treat(integrationRequest);
			integrationRequest.setIntegrationStatus(IntegrationStatus.OK);
		} else {
			integrationRequest.setIntegrationStatus(IntegrationStatus.KO);
		}
	}

	void treat(IntegrationRequestEntity integrationRequest) throws DataverseAPIException, DeleteApiException {
		final UUID globalId = integrationRequest.getGlobalId();

		try {
			final Metadata metadataToDelete = datasetService.getDataset(globalId);


			ProjectSearchCriteria criteria = new ProjectSearchCriteria();
			criteria.setDatasetUuids(List.of(globalId));

			Page<Project> projects = projectHelper.searchProjects(criteria, utilPageable.getPageable(0, 50, null));
			if(projects.hasContent()) {
				List<Project> projectList = projects.getContent();

				// On récupère l'ensemble des projets utilisant ce dataset
				while(projects.hasNext()) {
					projects = projectHelper.searchProjects(criteria, utilPageable.getPageable(projects.getNumber()+1, 50, null));
					projectList.addAll(projects.getContent());
				}

				for (Project p : projectList) {
					LinkedDataset linkedDatasetToUnlink = p.getLinkedDatasets().stream()
							.filter(ld -> ld.getDatasetUuid() != null && ld.getDatasetUuid().equals(metadataToDelete.getGlobalId()))
							.findFirst().orElseThrow(() -> new IllegalArgumentException("Dataset not found"));

					projectHelper.unlinkProjectToDataset(p.getUuid(), linkedDatasetToUnlink.getUuid(), true);

					emailHelper.sendDeleteDatasetEmail(p, metadataToDelete,  Locale.FRENCH);
				}
			}

			datasetService.archiveDataset(metadataToDelete.getDataverseDoi());

		} catch (final DatasetNotFoundException e) {
			log.info("Dataset {} to delete was not found", globalId);
		}

		apiGatewayManagerHelper.deleteApis(integrationRequest);
	}

	private boolean validateAndSetErrors(IntegrationRequestEntity integrationRequest) {
		final Set<IntegrationRequestErrorEntity> errors = new HashSet<>();
		UUID nodeProviderId = integrationRequest.getNodeProviderId();
		UUID datasetGlobalId = integrationRequest.getGlobalId();
		User user = isUserNodeProvider(nodeProviderId) ? aclHelper.getUserByLogin(nodeProviderId.toString()) : aclHelper.getUserByUUID(nodeProviderId);

		if (user == null) {
			errors.add(new IntegrationRequestErrorEntity(ERR_108.getCode(), ERR_108.getMessage()));
			integrationRequest.setErrors(errors);
			return errors.isEmpty();
		}

		Metadata datasetMetadata = null;
		// Test si le JDD existe
		try {
			datasetMetadata = datasetService.getDataset(datasetGlobalId);
		} catch (DatasetNotFoundException e) {
			errors.add(new IntegrationRequestErrorEntity(ERR_109.getCode(), ERR_109.getMessage()));
		} catch (DataverseAPIException e) {
			log.error("Erreur interne de la récupération du JDD d'UUID: {}", datasetGlobalId, e);
			errors.add(new IntegrationRequestErrorEntity(ERR_500.getCode(), ERR_500.getMessage()));
		}
		if (!errors.isEmpty()) {
			integrationRequest.setErrors(errors);
			return errors.isEmpty();
		}

		// Si l'utilisateur n'est pas un ADMIN/MODERATOR, on fait les autres vérifications
		if (!roleHelper.hasAnyRole(user, Role.ADMINISTRATOR, Role.MODERATOR)) {
			final Provider provider = providerHelper.getFullProviderByNodeProviderUUID(nodeProviderId);

			if (provider == null) {
				// Le node provider n'est lié a aucun provider
				errors.add(new IntegrationRequestErrorEntity(ERR_110.getCode(), ERR_110.getMessage()));
				integrationRequest.setErrors(errors);
				return errors.isEmpty();
			}

			// Le provider du nodeProvider n'est pas autorisé à réaliser cette demande
			if (!isSameProviderOrNull(datasetMetadata, provider)) {
				// le JDD est associé à un autre provider
				errors.add(new IntegrationRequestErrorEntity(ERR_111.getCode(), ERR_111.getMessage()));
			}

			// Le provider du nodeProvide n'est pas liée à l'organization producer
			if (!isLinkedToOrganization(datasetMetadata, provider)) {
				// le provider n'est pas associé à l'organisation producer du JDD
				errors.add(new IntegrationRequestErrorEntity(ERR_112.getCode(), ERR_112.getMessage()));
			}
		}

		// Sauvegarde des erreurs
		integrationRequest.setErrors(errors);

		return errors.isEmpty();
	}
}
