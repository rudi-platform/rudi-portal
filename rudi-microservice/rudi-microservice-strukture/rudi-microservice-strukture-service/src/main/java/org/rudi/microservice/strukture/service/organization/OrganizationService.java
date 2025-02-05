package org.rudi.microservice.strukture.service.organization;

import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceForbiddenException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.User;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.core.bean.OrganizationMember;
import org.rudi.microservice.strukture.core.bean.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.OrganizationUserMember;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationMembersSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des organisations
 *
 * @author FNI18300
 */
public interface OrganizationService {

	OwnerInfo getOrganizationOwnerInfo(UUID uuid) throws AppServiceBadRequestException, IllegalArgumentException;

	Organization getOrganization(UUID uuid) throws AppServiceNotFoundException;

	User getOrganizationUserFromOrganizationUuid(UUID organizationUuid)
			throws AppServiceNotFoundException, AppServiceUnauthorizedException, AppServiceForbiddenException;

	void updateOrganization(Organization organization) throws AppServiceException;

	void deleteOrganization(UUID uuid) throws AppServiceNotFoundException;

	Page<Organization> searchOrganizations(OrganizationSearchCriteria searchCriteria, Pageable pageable);

	OrganizationMember addOrganizationMember(UUID organizationUuid, OrganizationMember organizationMember)
			throws AppServiceException;

	List<OrganizationMember> getOrganizationMembers(UUID organizationUuid) throws AppServiceException;

	void removeOrganizationMembers(UUID organizationUuid, UUID userUuid) throws AppServiceException;

	Page<OrganizationUserMember> searchOrganizationMembers(OrganizationMembersSearchCriteria searchCriteria,
														   Pageable pageable) throws AppServiceException;

	Boolean isAuthenticatedOrganizationAdministrator(UUID organizationUuid) throws AppServiceException;

	OrganizationMember updateOrganizationMember(UUID organizationUuid, UUID userUuid,
												OrganizationMember organizationMember) throws AppServiceException;

	/**
	 * Create an organization
	 */

	Organization createOrganization(Organization organization) throws AppServiceBadRequestException;

}
