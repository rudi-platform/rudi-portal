package org.rudi.microservice.strukture.facade.controller;

import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODERATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_KALIM;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_STRUKTURE;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_STRUKTURE_ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.USER;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.kmedia.bean.KindOfData;
import org.rudi.microservice.strukture.core.bean.AbstractAddress;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Provider;
import org.rudi.microservice.strukture.core.bean.ProviderPageResult;
import org.rudi.microservice.strukture.core.bean.ProviderSearchCriteria;
import org.rudi.microservice.strukture.facade.controller.api.ProvidersApi;
import org.rudi.microservice.strukture.service.provider.ProviderService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProvidersController implements ProvidersApi {

	private final ProviderService providerService;
	private final UtilPageable utilPageable;
	private final ControllerHelper controllerHelper;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + "," + MODULE + ")")
	public ResponseEntity<ProviderPageResult> searchProviders(String code, String libelle, LocalDateTime dateDebut,
			java.time.LocalDateTime dateFin, UUID nodeProviderUuid, Boolean full, Integer offset, Integer limit,
			String order) {
		ProviderSearchCriteria searchCriteria = new ProviderSearchCriteria();
		searchCriteria.setCode(code);
		searchCriteria.setLabel(libelle);
		searchCriteria.setFull(full);
		searchCriteria.setNodeProviderUuid(nodeProviderUuid);
		searchCriteria.setDateDebut(dateDebut);
		searchCriteria.setDateFin(dateFin);

		Pageable pageable = utilPageable.getPageable(offset, limit, order);

		Page<Provider> page = providerService.searchProviders(searchCriteria, pageable);
		ProviderPageResult result = new ProviderPageResult();
		result.setTotal(page.getTotalElements());
		result.setElements(page.getContent());

		return ResponseEntity.ok(result);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<AbstractAddress> createAddress(UUID providerUuid, AbstractAddress abstractAddress) {
		return ResponseEntity.ok(providerService.createAddress(providerUuid, abstractAddress));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<NodeProvider> createNode(UUID providerUuid, NodeProvider nodeProvider) {
		return ResponseEntity.ok(providerService.createNode(providerUuid, nodeProvider));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<Provider> createProvider(Provider provider) {
		return ResponseEntity.ok(providerService.createProvider(provider));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<Void> deleteAddress(UUID providerUuid, UUID addressUuid) {
		providerService.deleteAddress(providerUuid, addressUuid);
		return ResponseEntity.ok().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<Void> deleteNode(UUID providerUuid, UUID nodeUuid) {
		providerService.deleteNode(providerUuid, nodeUuid);
		return ResponseEntity.ok().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<Void> deleteProvider(UUID providerUuid) {
		providerService.deleteProvider(providerUuid);
		return ResponseEntity.ok().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ", "
			+ MODULE_STRUKTURE + ", " + USER + ")")
	public ResponseEntity<Void> deleteProviderMediaByType(UUID providerUuid, KindOfData kindOfData)
			throws AppServiceException {
		providerService.deleteMedia(providerUuid, kindOfData);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Resource> downloadProviderMediaByType(UUID providerUuid, KindOfData kindOfData)
			throws Exception {
		final DocumentContent documentContent = providerService.downloadMedia(providerUuid, kindOfData);
		return controllerHelper.downloadableResponseEntity(documentContent);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + "," + MODULE + ")")
	public ResponseEntity<AbstractAddress> getAddress(UUID providerUuid, UUID addressUuid) {
		return ResponseEntity.ok(providerService.getAddress(providerUuid, addressUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + "," + MODULE + ")")
	public ResponseEntity<List<AbstractAddress>> getAddresses(UUID providerUuid) {
		return ResponseEntity.ok(providerService.getAddresses(providerUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + "," + MODULE + ")")
	public ResponseEntity<NodeProvider> getNode(UUID providerUuid, UUID nodeUuid) {
		return ResponseEntity.ok(providerService.getNode(providerUuid, nodeUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + "," + MODULE + ")")
	public ResponseEntity<List<NodeProvider>> getNodes(UUID providerUuid) {
		return ResponseEntity.ok(providerService.getNodes(providerUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + "," + MODULE + ")")
	public ResponseEntity<Provider> getProvider(UUID providerUuid, Boolean full) throws Exception {
		return ResponseEntity.ok(providerService.getProvider(providerUuid, full != null && full));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + "," + MODULE_KALIM + ")")
	public ResponseEntity<NodeProvider> patchNode(UUID providerUuid, UUID nodeUuid, LocalDateTime lastHarvestingDate) {
		return ResponseEntity.ok(providerService.patchNode(providerUuid, nodeUuid, lastHarvestingDate));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<AbstractAddress> updateAddress(UUID providerUuid, AbstractAddress abstractAddress) {
		return ResponseEntity.ok(providerService.updateAddress(providerUuid, abstractAddress));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<NodeProvider> updateNode(UUID providerUuid, NodeProvider nodeProvider) {
		return ResponseEntity.ok(providerService.updateNode(providerUuid, nodeProvider));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<Provider> updateProvider(Provider provider) {
		return ResponseEntity.ok(providerService.updateProvider(provider));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ", "
			+ MODULE_STRUKTURE + ", " + USER + ")")
	public ResponseEntity<Void> uploadProviderMediaByType(UUID providerUuid, KindOfData kindOfData, Resource body)
			throws Exception {
		providerService.uploadMedia(providerUuid, kindOfData, body);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
