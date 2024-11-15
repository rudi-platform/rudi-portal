package org.rudi.common.service.datafactory;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.storage.dao.StampedRepository;
import org.rudi.common.storage.entity.AbstractStampedEntity;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@RequiredArgsConstructor
public class AbstractStampedDataFactory<E extends AbstractStampedEntity, R extends StampedRepository<E>> extends AbstractDataFactory {

	protected final R repository;
	private final Class<E> type;

	public E getOrCreate(String code, String label, int order, LocalDateTime openingDate, LocalDateTime closingDate) {
		List<E> items = repository.findByCode(code, null);
		if (CollectionUtils.isNotEmpty(items)) {
			return items.get(0);
		}
		try {
			Constructor<E> constructor = type.getConstructor(new Class<?>[0]);
			E item = constructor.newInstance(new Object[0]);
			item.setUuid(UUID.randomUUID());
			item.setCode(code);
			item.setLabel(label != null ? label : code);
			item.setOrder(order);
			if (openingDate != null) {
				openingDate = openingDate.truncatedTo(ChronoUnit.MICROS);
			}
			item.setOpeningDate(openingDate);
			if (closingDate != null) {
				closingDate = closingDate.truncatedTo(ChronoUnit.MICROS);
			}
			item.setClosingDate(closingDate);
			assignData(item);
			return repository.save(item);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create item for " + type, e);
		}
	}

	protected void assignData(E item) {
	}

	public E getOrCreate(String code, String label) {
		return getOrCreate(code, label, 10, LocalDateTime.now(ZoneOffset.UTC), null);
	}

	public long countAll() {
		return repository.count();
	}

	protected void assign(AbstractStampedEntity entity, String code, String label) {
		entity.setUuid(UUID.randomUUID());
		entity.setLabel(label);
		entity.setCode(code);
		entity.setOpeningDate(LocalDateTime.of(2021, 02, 17, 15, 35, 17));
	}
}
