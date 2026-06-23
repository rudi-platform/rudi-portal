package org.rudi.microservice.projekt.facade.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.OwnerInfo;
import org.rudi.microservice.projekt.core.bean.OwnerInfoRequest;
import org.rudi.microservice.projekt.core.bean.OwnerType;
import org.rudi.microservice.projekt.facade.controller.api.OwnerInfoApi;
import org.rudi.microservice.projekt.facade.controller.api.OwnersInfosApi;
import org.rudi.microservice.projekt.service.ownerinfo.OwnerInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OwnerInfoController implements OwnerInfoApi, OwnersInfosApi {
	private final OwnerInfoService ownerInfoService;

	@Override
	public Optional<NativeWebRequest> getRequest() {
		return OwnerInfoApi.super.getRequest();
	}

	@Override
	public ResponseEntity<OwnerInfo> getOwnerInfo(OwnerType ownerType, UUID ownerUuid) throws Exception {
		return ResponseEntity.ok(ownerInfoService.getOwnerInfo(ownerType, ownerUuid));
	}

	@Override
	public ResponseEntity<Boolean> hasAccessToDataset(UUID uuidToCheck, UUID datasetUuid) throws Exception {
		return ResponseEntity.ok(ownerInfoService.hasAccessToDataset(uuidToCheck, datasetUuid));
	}

	@Override
	public ResponseEntity<UUID> getLinkedDatasetOwner(UUID linkedDatasetUuid) throws Exception {
		return ResponseEntity.ok(ownerInfoService.getLinkedDatasetOwner(linkedDatasetUuid));
	}

	@Override
	public ResponseEntity<List<OwnerInfo>> getOwnersInfos(List<OwnerInfoRequest> ownerInfoRequests) throws Exception {
		return ResponseEntity.ok(ownerInfoService.getOwnersInfos(ownerInfoRequests));
	}
}
