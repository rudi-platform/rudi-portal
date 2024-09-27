package org.rudi.facet.rva;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.facet.rva.exception.ExternalApiRvaException;
import org.rudi.facet.rva.exception.TooManyAddressesException;
import org.rudi.rva.core.bean.Address;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;

@RvaSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AddressServiceTI {

	private final AddressService addressService;

	@DisplayName("Taille de query trop petite")
	@Test
	void rva_searchAddresses_OneCharacterQuery() {
		assertThatThrownBy(() -> addressService.searchAddresses("a", null, null))
				.isInstanceOf(AppServiceBadRequestException.class);
	}

	@DisplayName("Aucune addresse ne matche avec la query")
	@Test
	void rva_searchAddresses_IncorrectAddress()
			throws ExternalApiRvaException, AppServiceBadRequestException, TooManyAddressesException {
		assertThat(addressService.searchAddresses("spod", null, null)).isEmpty();
	}

	@DisplayName(" L'API renvoie bien des addresses")
	@Test
	void rva_searchAddresses_RightAddress()
			throws ExternalApiRvaException, AppServiceBadRequestException, TooManyAddressesException {
		assertThat(addressService.searchAddresses("103 Boulevard", null, null)).isNotEmpty();
	}

	@Test
	void rva_searchAddresses_BUSINESS_ERROR() {
		assertThatThrownBy(() -> addressService.searchAddresses("6 ru", null, null))
				.isInstanceOf(TooManyAddressesException.class);
	}

	@Test
	void rva_searchAddresses()
			throws ExternalApiRvaException, TooManyAddressesException, AppServiceBadRequestException {
		List<Address> addresses = addressService.searchAddresses("103 boulevard", null, 20);
		assertNotNull(addresses);
		assertTrue(addresses.size() > 0);
		Address address = addressService.getAddressById(addresses.get(0).getId());
		assertNotNull(address);
		assertEquals(address.getId(), addresses.get(0).getId());
	}
}
