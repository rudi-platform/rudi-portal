/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.osm;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.facet.rva.MonoUtils;
import org.rudi.facet.rva.exception.ExternalApiRvaException;
import org.rudi.facet.rva.exception.TooManyAddressesException;
import org.rudi.facet.rva.impl.AbstractAddressServiceImpl;
import org.rudi.facet.rva.impl.osm.bean.OsmAddress;
import org.rudi.rva.core.bean.Address;
import org.rudi.rva.core.bean.AddressKind;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Exemple : https://nominatim.openstreetmap.org/search.php?q=belle
 * épine&polygon_geojson=1&viewbox=1.51863,47.02888,1.52625,47.02681&countrycodes=fr&limit=2&format=jsonv2
 * 
 * https://nominatim.openstreetmap.org/search.php?street=belle épine&city=cesson
 * sévigné&county=France&polygon_geojson=1&viewbox=1.51863,47.02888,1.52625,47.02681&countrycodes=fr&limit=2&format=jsonv2
 * 
 * [{"place_id":245537626,"licence":"Data © OpenStreetMap contributors, ODbL 1.0.
 * http://osm.org/copyright","osm_type":"way","osm_id":24753159,"lat":"48.1247466","lon":"-1.5978147","category":"highway","type":"unclassified","place_rank":26,"importance":0.10000999999999993,"addresstype":"road","name":"Rue
 * de Belle Épine","display_name":"Rue de Belle Épine, Bourgchevreuil, Cesson-Sévigné, Rennes, Ille-et-Vilaine, Bretagne, France métropolitaine,
 * 35510,
 * France","boundingbox":["48.1247466","48.1249313","-1.5984990","-1.5968448"],"geojson":{"type":"LineString","coordinates":[[-1.5968448,48.1248297],[-1.5968944,48.1248468],[-1.5971393,48.1249313],[-1.5972891,48.1249079],[-1.5978147,48.1247466],[-1.5980261,48.124782],[-1.5980844,48.1247917],[-1.5983442,48.1248351],[-1.598481,48.1248594],[-1.598499,48.1248064]]}},{"place_id":245632899,"licence":"Data
 * © OpenStreetMap contributors, ODbL 1.0.
 * http://osm.org/copyright","osm_type":"way","osm_id":466412036,"lat":"48.1220131","lon":"-1.599858","category":"highway","type":"tertiary","place_rank":26,"importance":0.10000999999999993,"addresstype":"road","name":"Rue
 * de Belle Épine","display_name":"Rue de Belle Épine, Le Bourg, Bourgchevreuil, Cesson-Sévigné, Rennes, Ille-et-Vilaine, Bretagne, France
 * métropolitaine, 35510,
 * France","boundingbox":["48.1219931","48.1220305","-1.6000345","-1.5997401"],"geojson":{"type":"LineString","coordinates":[[-1.5997401,48.1220305],[-1.599858,48.1220131],[-1.5999371,48.1220051],[-1.5999999,48.1219976],[-1.6000345,48.1219931]]}}]
 * 
 * 
 * @author FNI18300
 *
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rudi.rva.implementation", havingValue = "osm")
public class OsmAddressServiceImpl extends AbstractAddressServiceImpl {

	private final WebClient osmWebClient;
	private final OsmProperties osmProperties;
	private final OsmAddressMapper addressMapper;

	@Override
	public Address getAddressById(String idAddress) throws ExternalApiRvaException, TooManyAddressesException {
		if (StringUtils.isEmpty(idAddress)) {
			throw new IllegalArgumentException("IdAdresse could not be null");
		}
		String[] idParts = idAddress.split("\\|");
		if (idParts.length != 2) {
			throw new IllegalArgumentException("IdAdresse is invalid");
		}
		String id = idParts[0].substring(0, 1).toUpperCase() + idParts[1];

		final Mono<List<OsmAddress>> responseMono = osmWebClient.get()
				.uri(uriBuilder -> uriBuilder.path(osmProperties.getGetPath())
						.queryParam("format", osmProperties.getFormat()).queryParam("osm_ids", id)
						// .queryParam("addressdetails", 1).queryParam("hierarchy", 0).queryParam("group_hierarchy", 1)
						.queryParam("polygon_geojson", 1).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<OsmAddress>>() {
				});
		List<OsmAddress> response = MonoUtils.blockOrThrow(responseMono);
		if (CollectionUtils.isNotEmpty(response)) {
			return addressMapper.apiToDto(response.get(0));
		} else {
			return null;
		}
	}

	@Override
	protected List<Address> getAddresses(String query, AddressKind kind, Integer limit)
			throws ExternalApiRvaException, TooManyAddressesException {
		final Mono<List<OsmAddress>> fullAddressesResponseMono = osmWebClient.get()
				.uri(uriBuilder -> uriBuilder.path(osmProperties.getSearchPath())
						.queryParamIfPresent("limit", Optional.ofNullable(limit))
						.queryParamIfPresent("type", Optional.ofNullable(convertType(kind)))
						.queryParam("format", osmProperties.getFormat())
						.queryParamIfPresent("countrycodes", computeCountryCodes())
						.queryParamIfPresent("limit", Optional.ofNullable(limit)).queryParam("q", query).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<OsmAddress>>() {
				});
		var response = MonoUtils.blockOrThrow(fullAddressesResponseMono);

		if (response == null) {
			return Collections.emptyList();
		}
		return addressMapper.apiToDtos(response);
	}

	@Override
	protected int getQueryMinLength() {
		return osmProperties.getQueryMinLength();
	}

	protected Optional<String> computeCountryCodes() {
		return Optional.ofNullable(
				StringUtils.isNotEmpty(osmProperties.getCountrycodes()) ? osmProperties.getCountrycodes() : null);
	}

	@Override
	protected String convertType(AddressKind kind) {
		return kind != null ? kind.name().toLowerCase() : null;
	}

}
