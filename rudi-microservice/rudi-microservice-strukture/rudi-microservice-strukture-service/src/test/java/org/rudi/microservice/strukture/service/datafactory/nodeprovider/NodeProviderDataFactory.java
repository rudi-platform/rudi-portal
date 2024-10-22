package org.rudi.microservice.strukture.service.datafactory.nodeprovider;

import java.time.LocalDateTime;
import java.util.UUID;

import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.microservice.strukture.storage.dao.provider.NodeProviderDao;
import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class NodeProviderDataFactory extends AbstractDataFactory {

	private final NodeProviderDao repository;

	/**
	 * @param uuid obligatoire
	 * @param openingDate obligatoire
	 * @param closingDate -
	 * @param version -
	 * @param url -
	 * @param harvestable obligatoire (default false)
	 * @param notifiable obligatoire (default false)
	 * @param harvestingCron -
	 * @param lastHarvestingDate -
	 * @return NodeProviderEntity créée ou existante
	 */
	public NodeProviderEntity getOrCreate(
			UUID uuid,
			LocalDateTime openingDate,
			LocalDateTime closingDate,
			String version,
			String url,
			Boolean harvestable,
			Boolean notifiable,
			String harvestingCron,
			LocalDateTime lastHarvestingDate
	) {
		NodeProviderEntity item = repository.findByUuid(uuid);
		if(item != null) {
			return item;
		}

		try{
			item = new NodeProviderEntity();

			item.setUuid(uuid);
			item.setOpeningDate(handleDate(openingDate));
			item.setClosingDate(handleDate(closingDate));
			item.setVersion(version);
			item.setUrl(url);
			item.setHarvestable(Boolean.TRUE.equals(harvestable));
			item.setNotifiable(Boolean.TRUE.equals(notifiable));
			item.setHarvestingCron(harvestingCron);
			item.setLastHarvestingDate(handleDate(lastHarvestingDate));

			return repository.save(item);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create item for NodeProviderEntity", e);
		}

	}

	public long countAll(){
		return repository.count();
	}

}
