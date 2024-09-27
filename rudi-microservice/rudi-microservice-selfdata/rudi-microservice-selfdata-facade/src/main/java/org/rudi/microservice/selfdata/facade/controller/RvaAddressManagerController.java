package org.rudi.microservice.selfdata.facade.controller;

import java.util.List;

import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.facet.rva.AddressService;
import org.rudi.facet.rva.exception.ExternalApiRvaException;
import org.rudi.facet.rva.exception.TooManyAddressesException;
import org.rudi.microservice.selfdata.facade.controller.api.RvaAddressManagerApi;
import org.rudi.rva.core.bean.Address;
import org.rudi.rva.core.bean.AddressKind;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RvaAddressManagerController implements RvaAddressManagerApi {

	private final AddressService addressService;

	@Value("${rudi.selfdata.rva.limit:20}")
	private int rvaLimit;

	@Override
	public ResponseEntity<List<Address>> searchAddresses(String query)
			throws ExternalApiRvaException, AppServiceBadRequestException, TooManyAddressesException {
		return ResponseEntity.ok(addressService.searchAddresses(query, AddressKind.HOUSE_NUMBER, rvaLimit));
	}

	@Override
	public ResponseEntity<Address> getAddressById(String addressId) throws Exception {
		return ResponseEntity.ok(addressService.getAddressById(addressId));
	}
}
