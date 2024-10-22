package org.rudi.microservice.strukture.service.datafactory.provider;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.tika.utils.StringUtils;
import org.rudi.common.service.datafactory.AbstractStampedDataFactory;
import org.rudi.microservice.strukture.service.datafactory.nodeprovider.NodeProviderDataFactory;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderDao;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class ProviderDataFactory extends AbstractStampedDataFactory<ProviderEntity, ProviderDao> {

	@Autowired
	private NodeProviderDataFactory	nodeProviderDataFactory;

	public ProviderDataFactory(ProviderDao repository) {
		super(repository, ProviderEntity.class);
	}

	private ProviderEntity create(String code, String label, int order, LocalDateTime openingDate, LocalDateTime closingDate, Set<NodeProviderEntity> nodeProviders) {
		ProviderEntity item =  getOrCreate(code, label, order, openingDate, closingDate);
		item.setNodeProviders(nodeProviders);

		return repository.save(item);
	}

	public ProviderEntity createProviderWithOneNode(String providerCode, UUID nodeProviderUuid){
		if(StringUtils.isEmpty(providerCode)){
			providerCode = "TEST";
		}
		if(nodeProviderUuid == null){
			nodeProviderUuid = UUID.randomUUID();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minusDays(1);
		Set<NodeProviderEntity> nodeProviders = new HashSet<NodeProviderEntity>();
		nodeProviders.add(nodeProviderDataFactory.getOrCreate(nodeProviderUuid, yesterday, null, "v1", "https://127.0.0.1", null, null, null, null));
		return create(providerCode, "test", 0, now, null, nodeProviders);
	}

	/**
	 * @param item
	 */
	@Override
	protected void assignData(ProviderEntity item) {
		if(item.getLinkedProducers() == null){
			item.setLinkedProducers(new HashSet<LinkedProducerEntity>());
		}
	}
}
