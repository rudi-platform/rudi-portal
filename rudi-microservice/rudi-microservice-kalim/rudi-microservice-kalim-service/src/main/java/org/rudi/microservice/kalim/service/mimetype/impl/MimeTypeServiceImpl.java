package org.rudi.microservice.kalim.service.mimetype.impl;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.kalim.core.bean.MimeType;
import org.rudi.microservice.kalim.core.bean.MimeTypeSearchCriteria;
import org.rudi.microservice.kalim.service.helper.MimeTypeHelper;
import org.rudi.microservice.kalim.service.mapper.MimeTypeMapper;
import org.rudi.microservice.kalim.service.mimetype.MimeTypeService;
import org.rudi.microservice.kalim.storage.dao.mimetype.MimeTypeCustomDao;
import org.rudi.microservice.kalim.storage.dao.mimetype.MimeTypeDao;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MimeTypeServiceImpl implements MimeTypeService {

	private final MimeTypeCustomDao mimeTypeCustomDao;
	private final MimeTypeDao mimeTypeDao;
	private final MimeTypeHelper mimeTypeHelper;
	private final MimeTypeMapper mimeTypeMapper;
	private final UtilPageable utilPageable;

	@Override
	public MimeType createAllowedMimeType(MimeType mimeType) {
		if (mimeType == null || StringUtils.isBlank(mimeType.getCode())) {
			throw new IllegalArgumentException("Le code d'un type de mime est obligatoire");
		}

		// On normalise le mimeType (pas de +crypt) avant le contrôle de doublon
		final String normalizedCode = mimeTypeHelper.getInitialMimeType(mimeType.getCode());
		if (StringUtils.isBlank(normalizedCode)) {
			throw new IllegalArgumentException("Le code d'un type de mime est obligatoire");
		}
		mimeType.setCode(normalizedCode);

		MimeTypeSearchCriteria criteria = MimeTypeSearchCriteria.builder().code(normalizedCode).active(true).build();
		Pageable pageable = utilPageable.getPageable(0, 1, null);

		if(mimeTypeCustomDao.searchMimeTypes(criteria, pageable).getTotalElements() > 0) {
			throw new IllegalArgumentException(String.format("Le code %s est déjà utilisé pour un type de mime actif", normalizedCode));
		}

		return mimeTypeMapper.entityToDto(mimeTypeDao.save(mimeTypeMapper.dtoToEntity(mimeType)));
	}

	@Override
	public void deleteAllowedMimeType(UUID uuid) {
		mimeTypeDao.delete(mimeTypeDao.findByUUID(uuid));
	}

	@Override
	public Page<MimeType> searchAllowedMimeTypes(MimeTypeSearchCriteria criteria, Pageable pageable) {
		return mimeTypeMapper.entitiesToDto(mimeTypeCustomDao.searchMimeTypes(criteria, pageable), pageable);
	}

	@Override
	public MimeType updateAllowedMimeType(UUID uuid, MimeType mimeType) {
		MimeTypeEntity mimeTypeEntity = mimeTypeDao.findByUUID(uuid);

		// On normalise le mimeType (pas de +crypt)
		mimeType.setCode(mimeTypeHelper.getInitialMimeType(mimeType.getCode()));

		if(!mimeTypeEntity.getCode().equals(mimeType.getCode())) {
			throw new IllegalArgumentException(
					String.format("Le code d'un type de mime ne peut pas être modifié (code actuel : %s)",
							mimeTypeEntity.getCode())
			);
		}

		mimeTypeMapper.dtoToEntity(mimeType, mimeTypeEntity);

		return mimeTypeMapper.entityToDto(mimeTypeDao.save(mimeTypeEntity));
	}
}
