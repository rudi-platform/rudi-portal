package org.rudi.microservice.strukture.service.organization.bean;

import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.microservice.strukture.core.bean.OrganizationBean;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationBeanService {

	Page<OrganizationBean> searchOrganizationBeans(OrganizationSearchCriteria criteria, Pageable pageable);

	Page<OrganizationBean> searchPublicOrganizationBeans(OrganizationSearchCriteria criteria, Pageable pageable);

	Page<OrganizationBean> searchMyOrganizationBeans(OrganizationSearchCriteria criteria, Pageable pageable) throws AppServiceUnauthorizedException;


}
