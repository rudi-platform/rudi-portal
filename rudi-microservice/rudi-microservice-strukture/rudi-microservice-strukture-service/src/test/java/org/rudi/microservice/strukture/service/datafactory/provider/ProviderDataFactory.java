package org.rudi.microservice.strukture.service.datafactory.provider;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tika.utils.StringUtils;
import org.rudi.common.service.datafactory.AbstractStampedDataFactory;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.AbstractAddressDataFactory;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.EmailAddressRoleDataFactory;
import org.rudi.microservice.strukture.service.datafactory.nodeprovider.NodeProviderDataFactory;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderDao;
import org.rudi.microservice.strukture.storage.entity.address.AbstractAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class ProviderDataFactory extends AbstractStampedDataFactory<ProviderEntity, ProviderDao> {

	public static final String ADDRESS_ROLE_CODE = "CONTACT";

	@Autowired
	private NodeProviderDataFactory nodeProviderDataFactory;
	@Autowired
	private LinkedProducerDataFactory linkedProducerDataFactory;
	@Autowired
	private final EmailAddressRoleDataFactory emailAddressRoleDataFactory;
	@Autowired
	private AbstractAddressDataFactory abstractAddressDataFactory;

	public ProviderDataFactory(ProviderDao repository, EmailAddressRoleDataFactory emailAddressRoleDataFactory) {
		super(repository, ProviderEntity.class);
		this.emailAddressRoleDataFactory = emailAddressRoleDataFactory;
	}

	private ProviderEntity create(String code, String label, int order, LocalDateTime openingDate, LocalDateTime closingDate, Set<NodeProviderEntity> nodeProviders, boolean needAddress) {
		ProviderEntity item = getOrCreate(code, label, order, openingDate, closingDate);
		item.setNodeProviders(nodeProviders);
		assignMissingDatas(item, needAddress);
		return repository.save(item);
	}

	public ProviderEntity createProvider(String providerCode, UUID nodeProviderUuid) {
		if (StringUtils.isEmpty(providerCode)) {
			providerCode = randomString(20);
		}
		if (nodeProviderUuid == null) {
			nodeProviderUuid = UUID.randomUUID();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minusDays(1);
		Set<NodeProviderEntity> nodeProviders = new HashSet<NodeProviderEntity>();
		nodeProviders.add(nodeProviderDataFactory.getOrCreate(nodeProviderUuid, yesterday, null, "v1", "https://127.0.0.1/" + randomString(10), null, null, null, null));
		return create(providerCode, "test", 0, now, null, nodeProviders, true);
	}

	public ProviderEntity createProviderWithoutAddress(String providerCode, UUID nodeProviderUuid) {
		if (StringUtils.isEmpty(providerCode)) {
			providerCode = randomString(20);
		}
		if (nodeProviderUuid == null) {
			nodeProviderUuid = UUID.randomUUID();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minusDays(1);
		Set<NodeProviderEntity> nodeProviders = new HashSet<NodeProviderEntity>();
		nodeProviders.add(nodeProviderDataFactory.getOrCreate(nodeProviderUuid, yesterday, null, "v1", "https://127.0.0.1/" + randomString(10), null, null, null, null));
		return create(providerCode, "test", 0, now, null, nodeProviders, false);
	}

	/**
	 * @param item
	 */
	@Override
	protected void assignData(ProviderEntity item) {
		if (item.getLinkedProducers() == null) {
			item.setLinkedProducers(new HashSet<LinkedProducerEntity>());
		}
	}

	private void assignMissingDatas(ProviderEntity item, boolean needAddress) {
		{
			if(item.getNodeProviders() == null){
				item.setNodeProviders(new HashSet<NodeProviderEntity>());
			}

			if(item.getNodeProviders().isEmpty()){
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime yesterday = now.minusDays(1);
				item.getNodeProviders().add(nodeProviderDataFactory.getOrCreate(UUID.randomUUID(), yesterday, null, "v1", "https://127.0.0.1/"+randomString(10), null, null, null, null));
			}

			if (item.getLinkedProducers().isEmpty()) {
				UUID nodeProviderUuid = item.getNodeProviders().iterator().next().getUuid();
				item.getLinkedProducers().add(linkedProducerDataFactory.createValidatedLinkedProducer(item.getUuid(), UUID.randomUUID(), nodeProviderUuid));
			}

			if (item.getAddresses() == null) {
				item.setAddresses(new HashSet<AbstractAddressEntity>());
			}
			if(needAddress){
				AddressRoleEntity addressRole = emailAddressRoleDataFactory.getOrCreate(ADDRESS_ROLE_CODE, ADDRESS_ROLE_CODE.toLowerCase());

				if (item.getAddresses().stream().filter(a -> a.getType().equals(AddressType.EMAIL) && a.getAddressRole().equals(addressRole)).collect(Collectors.toList()).isEmpty()) {
					item.getAddresses().add((EmailAddressEntity) abstractAddressDataFactory.createEmailAddress(ADDRESS_ROLE_CODE));
				}
			}
		}
	}

}
