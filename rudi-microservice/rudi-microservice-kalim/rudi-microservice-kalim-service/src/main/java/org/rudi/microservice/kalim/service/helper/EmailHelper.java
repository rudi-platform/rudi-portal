package org.rudi.microservice.kalim.service.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.micrometer.common.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.rudi.common.core.DocumentContent;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.email.model.EMailDescription;
import org.rudi.facet.generator.exception.GenerationException;
import org.rudi.facet.generator.exception.GenerationModelNotFoundException;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.organization.bean.OrganizationMember;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationMembersException;
import org.rudi.microservice.kalim.core.bean.DeleteDatasetTemplateDataModel;
import org.rudi.microservice.projekt.core.bean.OwnerType;
import org.rudi.microservice.projekt.core.bean.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailHelper {

	private final EMailService eMailService;
	private final ACLHelper aCLHelper;
	private final OrganizationHelper organizationHelper;

	@Value("${email.urlServer}")
	private String urlServer;

	@Value("${email.projectName}")
	private String projectName;

	@Value("${email.teamName}")
	private String teamName;

	@Value("${email.dataset.delete.subject}")
	private String deleteDatasetSubject;
	@Value("${email.dataset.delete.body}")
	private String deleteDatasetBody;


	@Getter(value = AccessLevel.PROTECTED)
	private final EMailService mailService;

	@Getter(value = AccessLevel.PROTECTED)
	private final TemplateGenerator templateGenerator;

	public void sendDeleteDatasetEmail(Project project, Metadata metadata, Locale locale) {
		try {
			Map<String, Object> additionalProperties = computeAdditionnalProperties(null);

			String producerName = metadata.getProducer().getOrganizationName();
			String datasetTitle = metadata.getResourceTitle();
			String projectTitle = project.getTitle();

			DeleteDatasetTemplateDataModel dataModelSubject = new DeleteDatasetTemplateDataModel(
					additionalProperties,
					locale,
					deleteDatasetSubject, producerName, datasetTitle, projectTitle
			);

			DeleteDatasetTemplateDataModel dataModelBody = new DeleteDatasetTemplateDataModel(
					additionalProperties,
					locale,
					deleteDatasetBody, producerName, datasetTitle, projectTitle
			);

			List<String> emailRecipients = computeEmailRecipients(project);
			for(String emailRecipient : emailRecipients){
				if(StringUtils.isNotEmpty(emailRecipient)){
					DocumentContent subject = templateGenerator.generateDocument(dataModelSubject);
					DocumentContent body = templateGenerator.generateDocument(dataModelBody);

					EMailDescription eMailDescription = new EMailDescription(
							emailRecipient,
							FileUtils.readFileToString(subject.getFile(), StandardCharsets.UTF_8),
							body
					);

					eMailService.sendMailAndCatchException(eMailDescription);
				}
			}

		} catch (GenerationException | IOException | GenerationModelNotFoundException | GetOrganizationMembersException e) {
			log.error("Cannot send dataset deletion mail", e);
		}
	}

	protected Map<String, Object> computeAdditionnalProperties(String pathServer) {
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("projectName", projectName);
		additionalProperties.put("teamName", teamName);
		additionalProperties.put("urlServer", urlServer);
		if (pathServer != null) {
			additionalProperties.put("pathServer", pathServer);
		}
		return additionalProperties;
	}

	private List<String> computeEmailRecipients(Project project) throws GetOrganizationMembersException {
		List<String> to = new ArrayList<>();

		UUID ownerUuid = project.getOwnerUuid();

		if(OwnerType.ORGANIZATION.equals(project.getOwnerType())) {
			Collection< OrganizationMember> organizationMembers = organizationHelper.getOrganizationMembers(ownerUuid);
			for (OrganizationMember om : organizationMembers) {
				User user = aCLHelper.getUserByUUID(om.getUserUuid());

				if(user != null) {
					to.add(aCLHelper.lookupEMailAddress(user));
				} else {
					log.error("Cannot find user for organization member {}", om.getUserUuid());
				}
			}
		}
		else {
			User user = aCLHelper.getUserByUUID(ownerUuid);

			if(user == null) {
				log.error("Cannot find user for project {}", project.getUuid());
				return to;
			}

			to.add(aCLHelper.lookupEMailAddress(user));
		}

		return to;
	}

}
