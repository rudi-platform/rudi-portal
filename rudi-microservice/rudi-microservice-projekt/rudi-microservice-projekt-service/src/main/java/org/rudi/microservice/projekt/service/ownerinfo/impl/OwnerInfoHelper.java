package org.rudi.microservice.projekt.service.ownerinfo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.core.bean.OwnerInfo;
import org.rudi.microservice.projekt.core.bean.OwnerType;

interface OwnerInfoHelper {
	boolean isHelperFor(OwnerType ownerType);

	OwnerInfo getOwnerInfo(UUID ownerUuid) throws AppServiceException;

	/**
	 * Récupère les infos de plusieurs owners du même type en un minimum d'appels.
	 * L'ordre de la liste retournée correspond à celui de la liste d'UUIDs fournie.
	 * Implémentation par défaut : appel unitaire par UUID.
	 */
	default List<OwnerInfo> getOwnersInfo(List<UUID> ownerUuids) throws AppServiceException {
		List<OwnerInfo> result = new ArrayList<>(ownerUuids.size());
		for (UUID uuid : ownerUuids) {
			result.add(getOwnerInfo(uuid));
		}
		return result;
	}
}
