package org.rudi.facet.rva.impl.rva;

import java.util.Collections;
import java.util.List;

import org.rudi.facet.rva.MonoUtils;
import org.rudi.facet.rva.exception.ExternalApiRvaException;
import org.rudi.facet.rva.exception.TooManyAddressesException;
import org.rudi.facet.rva.impl.AbstractAddressServiceImpl;
import org.rudi.facet.rva.impl.rva.bean.FullAddressesResponse;
import org.rudi.rva.core.bean.Address;
import org.rudi.rva.core.bean.AddressKind;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rudi.rva.implementation", havingValue = "rva", matchIfMissing = true)
public class RvaAddressServiceImpl extends AbstractAddressServiceImpl {
	private final WebClient rvaWebClient;
	private final RvaProperties rvaProperties;
	private final RvaAddressMapper addressMapper;

	@Override
	public Address getAddressById(String idAddress) throws ExternalApiRvaException, TooManyAddressesException {
		final Mono<FullAddressesResponse> fullAddressesResponseMono = rvaWebClient.get()
				.uri(uriBuilder -> uriBuilder.queryParam("key", rvaProperties.getKey())
						.queryParam("version", rvaProperties.getVersion()).queryParam("epsg", rvaProperties.getEpsg())
						.queryParam("format", rvaProperties.getFormat())
						.queryParam("cmd", rvaProperties.getCommandAddressById()).queryParam("idaddress", idAddress)
						.build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(FullAddressesResponse.class);
		var response = MonoUtils.blockOrThrow(fullAddressesResponseMono);
		if (response == null) {
			return null;
		}
		return addressMapper.apiToDto(response.getRva().getAnswer().getAddresses().get(0));
	}

	@Override
	protected List<Address> getAddresses(String query, AddressKind kind, Integer limit)
			throws ExternalApiRvaException, TooManyAddressesException {
		final Mono<FullAddressesResponse> fullAddressesResponseMono = rvaWebClient.get()
				.uri(uriBuilder -> uriBuilder.queryParam("key", rvaProperties.getKey())
						.queryParam("version", rvaProperties.getVersion()).queryParam("epsg", rvaProperties.getEpsg())
						.queryParam("format", rvaProperties.getFormat())
						.queryParam("cmd", rvaProperties.getCommandFullAddresses()).queryParam("query", query).build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(FullAddressesResponse.class);

		var response = MonoUtils.blockOrThrow(fullAddressesResponseMono);

		if (response == null) {
			return Collections.emptyList();
		}
		List<Address> result = addressMapper.apiToDtos(response.getRva().getAnswer().getAddresses());
		if (result != null && limit != null && result.size() > limit) {
			result = result.subList(0, limit);
		}
		return result;
	}

	@Override
	protected int getQueryMinLength() {
		return rvaProperties.getQueryMinLength();
	}

	@Override
	protected String convertType(AddressKind kind) {
		return null;
	}
}
