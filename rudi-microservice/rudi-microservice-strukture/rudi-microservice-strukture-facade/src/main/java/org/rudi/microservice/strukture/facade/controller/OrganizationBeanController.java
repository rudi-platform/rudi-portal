package org.rudi.microservice.strukture.facade.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.strukture.core.bean.OrganizationBean;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;
import org.rudi.microservice.strukture.core.bean.PagedOrganizationBeanList;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.facade.controller.api.MyOrganizationBeansApi;
import org.rudi.microservice.strukture.facade.controller.api.OrganizationBeansApi;
import org.rudi.microservice.strukture.facade.controller.api.PublicOrganizationBeansApi;
import org.rudi.microservice.strukture.service.organization.bean.OrganizationBeanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import lombok.RequiredArgsConstructor;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;

@RestController
@RequiredArgsConstructor
public class OrganizationBeanController implements OrganizationBeansApi, PublicOrganizationBeansApi, MyOrganizationBeansApi {

	private final OrganizationBeanService organizationBeanService;
	private final UtilPageable utilPageable;

	/**
	 * @return
	 */
	@Override
	public Optional<NativeWebRequest> getRequest() {
		return OrganizationBeansApi.super.getRequest();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ")")
	public ResponseEntity<PagedOrganizationBeanList> searchOrganizationsBeans(UUID userUuid,
			OrganizationStatus organizationStatus, Integer offset, Integer limit, String order) throws Exception {

		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().userUuid(userUuid).organizationStatus(List.of(organizationStatus)).build();

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);

		PagedOrganizationBeanList pagedOrganizationBeans = new PagedOrganizationBeanList();
		pagedOrganizationBeans.setElements(organizationBeans.getContent());
		pagedOrganizationBeans.setTotal(organizationBeans.getTotalElements());

		return ResponseEntity.ok(pagedOrganizationBeans);
	}


	@Override
	public ResponseEntity<PagedOrganizationBeanList> searchPublicOrganizationsBeans(String name, List<UUID> uuids, List<UUID> excludedOrganizationUuids ,Boolean full, Boolean active, Integer offset, Integer limit, String order) throws Exception {
		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.name(name)
				.active(active)
				.uuids(uuids)
				.excludeOrganizationUuids(excludedOrganizationUuids)
				.loadAllInformations(full)
				.build();
		Page<OrganizationBean> organizationBeans = organizationBeanService.searchPublicOrganizationBeans(criteria, pageable);
		return ResponseEntity.ok(new PagedOrganizationBeanList().elements(organizationBeans.getContent()).total(organizationBeans.getTotalElements()));
	}

	@Override
	public ResponseEntity<PagedOrganizationBeanList> searchMyOrganizationBeans(List<UUID> uuids, String name, Boolean full, Boolean active, Integer offset, Integer limit, String order) throws Exception {
		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().uuids(uuids).name(name).loadAllInformations(Boolean.TRUE.equals(full)).active(active).build();
		Page<OrganizationBean> organizationBeans = organizationBeanService.searchMyOrganizationBeans(criteria, pageable);
		return ResponseEntity.ok(new PagedOrganizationBeanList().elements(organizationBeans.getContent()).total(organizationBeans.getTotalElements()));
	}
}
