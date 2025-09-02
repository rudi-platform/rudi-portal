package org.rudi.facet.providers.helper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.providers.bean.NodeProvider;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.bean.ProviderPageResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

/**
 * L'utilisation de ce helper requiert l'ajout de 2 propriétés dans le fichier de configuration associé
 *
 * @author FNI18300
 */
@Component
@Slf4j
public class ProviderHelper {

	private static final String NODE_PROVIDER_UUID_PARAMETER = "nodeProviderUuids";
	private static final String FULL_PARAMETER = "full";
	private static final String LIMIT_PARAMETER = "limit";

	@Getter
	private final String providersEndpointSearchURL;
	@Getter
	private final String providersEndpointGetURL;
	private final String providersEndpointPatchNodeUrl;
	private final WebClient loadBalancedWebClient;

	@Getter
	private final UUID defaultProviderUuid;

	public ProviderHelper(
			@Value("${rudi.facet.providers.endpoint.search.url:/strukture/v1/providers}") String providersEndpointSearchURL,
			@Value("${rudi.facet.providers.endpoint.get.url:/strukture/v1/providers}") String providersEndpointGetURL,
			@Value("${rudi.facet.providers.endpoint.nodes.patch.url:/strukture/v1/providers/{providerUuid}/nodes/{nodeUuid}}") String providersEndpointPatchNodeUrl,
			@Value("${rudi.facet.providers.service.url:lb://RUDI-STRUKTURE/}") String struktureServiceURL,
			@Value("${rudi.facet.providers.default-provider-uuid:5596b5b2-b227-4c74-a9a1-719e7c1008c7}") UUID defaultProviderUuid,
			@Qualifier("rudi_oauth2_builder") WebClient.Builder webClientBuilder) {
		this.providersEndpointSearchURL = providersEndpointSearchURL;
		this.providersEndpointGetURL = providersEndpointGetURL;
		this.providersEndpointPatchNodeUrl = providersEndpointPatchNodeUrl;
		this.defaultProviderUuid = defaultProviderUuid;
		this.loadBalancedWebClient = webClientBuilder.baseUrl(struktureServiceURL).build();
	}

	/**
	 * Accède au service µProviders pour trouver un provider par son uuid
	 *
	 * @param providerUuid uuid recherché
	 * @return le provider
	 */
	public Provider getProviderByUUID(UUID providerUuid) {
		if (providerUuid == null) {
			throw new IllegalArgumentException("provider uuid required");
		}
		return loadBalancedWebClient.get().uri(uriBuilder -> buildGetURL(uriBuilder, providerUuid)).retrieve()
				.bodyToMono(Provider.class).block();
	}

	/**
	 * Accède au service µProviders pour trouver un provider par l'uuid d'un de ses noeuds
	 *
	 * @param nodeProviderUUId uuid du noeud recherché
	 * @return le provider correspondant
	 */
	@Nullable
	public Provider getProviderByNodeProviderUUID(UUID nodeProviderUUId) {
		Provider result = null;
		if (nodeProviderUUId == null) {
			throw new IllegalArgumentException("node provider uuid required");
		}

		ProviderPageResult pageResult = loadBalancedWebClient.get()
				.uri(uriBuilder -> searchUriBuilder(uriBuilder).queryParam(LIMIT_PARAMETER, 1)
						.queryParam(NODE_PROVIDER_UUID_PARAMETER, List.of(nodeProviderUUId)).build())
				.retrieve().bodyToMono(ProviderPageResult.class).block();
		if (pageResult != null && CollectionUtils.isNotEmpty(pageResult.getElements())) {
			result = pageResult.getElements().get(0);
		}
		return result;
	}

	/**
	 * Accède au service µProviders pour trouver un provider FULL par l'uuid d'un de ses noeuds
	 *
	 * @param nodeProviderUUId uuid du noeud recherché
	 * @return le provider correspondant
	 */
	@Nullable
	public Provider getFullProviderByNodeProviderUUID(UUID nodeProviderUUId) {
		Provider result = null;
		if (nodeProviderUUId == null) {
			throw new IllegalArgumentException("node provider uuid required");
		}

		ProviderPageResult pageResult = loadBalancedWebClient.get()
				.uri(uriBuilder -> searchUriBuilder(uriBuilder).queryParam(LIMIT_PARAMETER, 1)
						.queryParam(NODE_PROVIDER_UUID_PARAMETER,  List.of(nodeProviderUUId)).queryParam(FULL_PARAMETER, true)
						.build())
				.retrieve().bodyToMono(ProviderPageResult.class).block();
		if (pageResult != null && CollectionUtils.isNotEmpty(pageResult.getElements())) {
			result = pageResult.getElements().get(0);
		}
		return result;
	}

	/**
	 * Version non null de {@link #getProviderByNodeProviderUUID(UUID)}.
	 *
	 * @throws NullPointerException si le fournisseur est introuvable
	 */
	@Nonnull
	public Provider requireProviderByNodeProviderUUID(UUID nodeProviderUUId) {
		final var provider = getProviderByNodeProviderUUID(nodeProviderUUId);
		return Objects.requireNonNull(provider,
				"Fournisseur introuvable pour l'UUID de nœud fournisseur " + nodeProviderUUId);
	}

	/**
	 * Accède au service µProviders pour trouver un noeud par son uuid
	 *
	 * @param nodeProviderUUId uuid du noeud recherché
	 * @return le noeud correspondant
	 */
	@Nullable
	public NodeProvider getNodeProviderByUUID(UUID nodeProviderUUId) {
		NodeProvider result = null;
		Provider provider = getProviderByNodeProviderUUID(nodeProviderUUId);
		if (provider != null && CollectionUtils.isNotEmpty(provider.getNodeProviders())) {
			result = provider.getNodeProviders().stream().filter(n -> n.getUuid().equals(nodeProviderUUId)).findFirst()
					.orElse(null);
		}
		return result;
	}

	/**
	 * Version non null de {@link #getNodeProviderByUUID(UUID)}.
	 *
	 * @throws NullPointerException si le nœud fournisseur est introuvable
	 */
	@Nonnull
	public NodeProvider requireNodeProviderByUUID(UUID nodeProviderUUId) {
		final var nodeProvider = getNodeProviderByUUID(nodeProviderUUId);
		return Objects.requireNonNull(nodeProvider, "Nœud fournisseur introuvable à l'UUID " + nodeProviderUUId);
	}

	protected URI buildGetURL(UriBuilder uriBuilder, UUID value) {
		return uriBuilder.path(getProvidersEndpointGetURL()).pathSegment(value.toString()).build();
	}

	protected UriBuilder searchUriBuilder(UriBuilder uriBuilder) {
		return uriBuilder.path(getProvidersEndpointSearchURL()).queryParam(FULL_PARAMETER, true);
	}

	/**
	 * Accède au service µProviders pour trouver la liste de tous les noeuds fournisseurs correspondants aux critères
	 */
	public Mono<List<Provider>> getAllProviders() {
		final Mono<ProviderPageResult> pageResultMono = loadBalancedWebClient.get()
				.uri(uriBuilder -> searchUriBuilder(uriBuilder).build()).retrieve()
				.bodyToMono(ProviderPageResult.class);
		return pageResultMono.map(pageResult -> {
			if (pageResult != null && CollectionUtils.isNotEmpty(pageResult.getElements())) {
				return pageResult.getElements();
			} else {
				return Collections.emptyList();
			}
		});
	}

	public Mono<List<NodeWithProviderUuid>> getHarvestableNodes() {
		return getAllProviders().map(providers -> providers.stream()
				.filter(provider -> CollectionUtils.isNotEmpty(provider.getNodeProviders()))
				.flatMap(provider -> provider.getNodeProviders().stream()
						.map(node -> new NodeWithProviderUuid(provider.getUuid(), node)))
				.filter(nodeWithProviderUuid -> this.isHarvestable(nodeWithProviderUuid.getNode()))
				.collect(Collectors.toList()));
	}

	private boolean isHarvestable(NodeProvider nodeProvider) {
		return isTrue(nodeProvider.getHarvestable());
	}

	public Mono<NodeProvider> patchNode(UUID providerUuid, UUID nodeUuid, LocalDateTime lastHarvestingDate) {
		return loadBalancedWebClient.patch()
				.uri(uriBuilder -> uriBuilder.path(providersEndpointPatchNodeUrl)
						.queryParam("lastHarvestingDate", lastHarvestingDate).build(providerUuid, nodeUuid))
				.retrieve().bodyToMono(NodeProvider.class);
	}
}
