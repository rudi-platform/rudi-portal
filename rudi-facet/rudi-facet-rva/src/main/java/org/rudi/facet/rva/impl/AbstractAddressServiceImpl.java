/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.facet.rva.AddressService;
import org.rudi.facet.rva.exception.ExternalApiRvaException;
import org.rudi.facet.rva.exception.TooManyAddressesException;
import org.rudi.rva.core.bean.Address;
import org.rudi.rva.core.bean.AddressKind;

/**
 * @author FNI18300
 *
 */
public abstract class AbstractAddressServiceImpl implements AddressService {

	@Override
	public List<Address> searchAddresses(String query, AddressKind kind, Integer limit)
			throws ExternalApiRvaException, AppServiceBadRequestException, TooManyAddressesException {
		if (StringUtils.isEmpty(query) || query.length() < getQueryMinLength()) {
			throw new AppServiceBadRequestException("Missing query parameter or query too short");
		}
		return getAddresses(query, kind, limit);
	}

	protected abstract List<Address> getAddresses(String query, AddressKind kind, Integer limit)
			throws ExternalApiRvaException, TooManyAddressesException;

	protected abstract int getQueryMinLength();

	protected abstract String convertType(AddressKind kind);

}
