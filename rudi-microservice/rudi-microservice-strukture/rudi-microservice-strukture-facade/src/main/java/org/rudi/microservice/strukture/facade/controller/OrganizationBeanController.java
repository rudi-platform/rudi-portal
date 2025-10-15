package org.rudi.microservice.strukture.facade.controller;

import java.util.UUID;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.strukture.core.bean.OrganizationBean;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;
import org.rudi.microservice.strukture.core.bean.PagedOrganizationBeanList;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.facade.controller.api.OrganizationBeansApi;
import org.rudi.microservice.strukture.service.organization.bean.OrganizationBeanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizationBeanController implements OrganizationBeansApi {

	private final OrganizationBeanService organizationBeanService;
	private final UtilPageable utilPageable;

	@Override
	public ResponseEntity<PagedOrganizationBeanList> searchOrganizationsBeans(UUID userUuid,
			OrganizationStatus organizationStatus, Integer offset, Integer limit, String order) throws Exception {

		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().userUuid(userUuid).organizationStatus(organizationStatus).build();

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);

		PagedOrganizationBeanList pagedOrganizationBeans = new PagedOrganizationBeanList();
		pagedOrganizationBeans.setElements(organizationBeans.getContent());
		pagedOrganizationBeans.setTotal(organizationBeans.getTotalElements());

		return ResponseEntity.ok(pagedOrganizationBeans);
	}
}
