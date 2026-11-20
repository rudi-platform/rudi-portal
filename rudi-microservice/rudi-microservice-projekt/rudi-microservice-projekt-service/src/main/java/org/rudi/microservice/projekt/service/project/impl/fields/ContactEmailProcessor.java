package org.rudi.microservice.projekt.service.project.impl.fields;

import javax.annotation.Nullable;

import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.facade.config.filter.CommonConstants;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ContactEmailProcessor implements CreateProjectFieldProcessor, UpdateProjectFieldProcessor {
	private final ACLHelper aclHelper;
	private final UtilContextHelper utilContextHelper;

	@Override
	public void process(@Nullable ProjectEntity project, ProjectEntity existingProject) throws AppServiceException {
		if(project != null) {
			if (project.getContactEmail() == null) {
				AuthenticatedUser authenticatedUser = utilContextHelper.getAuthenticatedUser();
				if (authenticatedUser != null) {
					project.setContactEmail(getContactEmailFromUserLogin(authenticatedUser.getLogin()));
				}
			} else if (!project.getContactEmail().matches(CommonConstants.EMAIL_REGEX)) {
				throw new AppServiceException("Le mail de contact n'est pas valide");
			}
		}
	}

	private String getContactEmailFromUserLogin(String login) {
		if (login != null) {
			final User user = aclHelper.getUserByLogin(login);
			if (user != null) {
				return aclHelper.lookupEMailAddress(user);
			}
		}
		return null;
	}

}
