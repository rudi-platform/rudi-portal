package org.rudi.facet.rva.impl.ban;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.facet.rva.MonoUtils;
import org.rudi.facet.rva.exception.ExternalApiRvaException;
import org.rudi.facet.rva.exception.TooManyAddressesException;
import org.rudi.facet.rva.impl.AbstractAddressServiceImpl;
import org.rudi.facet.rva.impl.ban.bean.BanContext;
import org.rudi.facet.rva.impl.ban.bean.BanResponse;
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
@ConditionalOnProperty(name = "rudi.rva.implementation", havingValue = "ban")
public class BanAddressServiceImpl extends AbstractAddressServiceImpl {

	private final WebClient banWebClient;
	private final BanProperties banProperties;
	private final BanAddressMapper addressMapper;

	@Override
	protected List<Address> getAddresses(String query, AddressKind kind, Integer limit)
			throws ExternalApiRvaException, TooManyAddressesException {

		final Mono<BanResponse> fullAddressesResponseMono = banWebClient.get()
				.uri(uriBuilder -> uriBuilder.queryParamIfPresent("limit", Optional.ofNullable(limit))
						.queryParamIfPresent("type", Optional.ofNullable(convertType(kind))).queryParam("q", query)
						.build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(BanResponse.class);

		var response = MonoUtils.blockOrThrow(fullAddressesResponseMono);

		if (response == null) {
			return Collections.emptyList();
		}
		BanContext context = BanContext.builder().query(query).limit(limit).type(convertType(kind)).build();

		return addressMapper.apiToDtos(response.getFeatures(), context);
	}

	@Override
	public Address getAddressById(String idAddress) throws ExternalApiRvaException, TooManyAddressesException {
		Address result = null;
		String[] idAddressParts = idAddress.split("\\|");
		List<Address> addresses = getAddresses(extractQuery(idAddressParts[1]), extractKind(idAddressParts[2]),
				extractLimit(idAddressParts[3]));
		if (CollectionUtils.isNotEmpty(addresses)) {
			result = addresses.stream().filter(address -> address.getId().equals(idAddress)).findFirst().orElse(null);
		}
		return result;
	}

	protected Integer extractLimit(String value) {
		return StringUtils.isNotEmpty(value) && !"null".equals(value) ? Integer.valueOf(value) : null;
	}

	protected AddressKind extractKind(String value) {
		return StringUtils.isNotEmpty(value) ? AddressKind.valueOf(value.toUpperCase()) : null;
	}

	protected String extractQuery(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	@Override
	protected int getQueryMinLength() {
		return banProperties.getQueryMinLength();
	}

	@Override
	protected String convertType(AddressKind kind) {
		return kind != null ? kind.name().toLowerCase() : null;
	}

}
