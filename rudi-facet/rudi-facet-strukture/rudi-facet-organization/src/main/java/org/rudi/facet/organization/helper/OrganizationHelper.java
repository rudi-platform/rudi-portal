package org.rudi.facet.organization.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.bean.OrganizationMember;
import org.rudi.facet.organization.bean.OrganizationRole;
import org.rudi.facet.organization.bean.OrganizationStatus;
import org.rudi.facet.organization.bean.PagedOrganizationList;
import org.rudi.facet.organization.helper.exceptions.AddUserToOrganizationException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationMembersException;
import org.rudi.facet.strukture.MonoUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrganizationHelper {

	private final OrganizationProperties organizationProperties;
	private final WebClient organizationWebClient;

	public boolean organizationContainsUser(UUID organizationUuid, UUID userUuid)
			throws GetOrganizationMembersException {
		if (organizationUuid == null) {
			throw new IllegalArgumentException("organizationUuid required");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid required");
		}
		final var members = getOrganizationMembers(organizationUuid);
		if (members == null) {
			return false;
		}
		return members.stream().anyMatch(member -> member.getUserUuid().equals(userUuid));
	}

	public boolean organizationContainsUserAsAdministrator(UUID organizationUuid, UUID userUuid)
			throws GetOrganizationMembersException {
		if (organizationUuid == null) {
			throw new IllegalArgumentException("organizationUuid required");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid required");
		}
		final var members = getOrganizationMembers(organizationUuid);
		if (members == null) {
			return false;
		}
		return members.stream().anyMatch(member ->
				member.getUserUuid().equals(userUuid)
						&& member.getRole().equals(OrganizationRole.ADMINISTRATOR)
		);
	}

	public OrganizationMember addMemberToOrganization(OrganizationMember member, UUID organizationUuid)
			throws AddUserToOrganizationException {
		var mono = organizationWebClient.post()
				.uri(uriBuilder -> uriBuilder.path(organizationProperties.getMembersPath()).build(organizationUuid))
				.body(Mono.just(member), OrganizationMember.class).retrieve().bodyToMono(OrganizationMember.class);
		return MonoUtils.blockOrThrow(mono, AddUserToOrganizationException.class);
	}

	public OrganizationMember addMemberToOrganizationIfNotExists(OrganizationMember member, UUID organizationUuid)
			throws AddUserToOrganizationException, GetOrganizationMembersException {

		// Récupération des membres de l'organisation
		final var members = getOrganizationMembers(organizationUuid);

		// On vérifie si l'utilisateur est déjà présent avant d'essayer de l'ajouter
		if (!members.isEmpty()) {
			Optional<OrganizationMember> existingMember = members
					.stream()
					.filter(m -> member.getUserUuid().equals(m.getUserUuid())).findFirst();

			if (existingMember.isPresent()) {
				return existingMember.get();
			}
		}
		var mono = organizationWebClient.post()
				.uri(uriBuilder -> uriBuilder.path(organizationProperties.getMembersPath()).build(organizationUuid))
				.body(Mono.just(member), OrganizationMember.class).retrieve().bodyToMono(OrganizationMember.class);
		return MonoUtils.blockOrThrow(mono, AddUserToOrganizationException.class);
	}

	public Collection<OrganizationMember> getOrganizationMembers(UUID organizationUuid)
			throws GetOrganizationMembersException {
		final var mono = organizationWebClient.get()
				.uri(uriBuilder -> uriBuilder.path(organizationProperties.getMembersPath()).build(organizationUuid))
				.retrieve().bodyToMono(new ParameterizedTypeReference<List<OrganizationMember>>() {
				});
		return MonoUtils.blockOrThrow(mono, GetOrganizationMembersException.class);
	}

	/**
	 * @return null si l'organisation n'a pas été trouvée
	 */
	@Nullable
	public Organization getOrganization(UUID organizationUuid) throws GetOrganizationException {
		final var mono = organizationWebClient.get()
				.uri(uriBuilder -> uriBuilder.path(organizationProperties.getOrganizationsPath())
						.queryParam("uuid", organizationUuid).build())
				.retrieve().bodyToMono(PagedOrganizationList.class);
		final var pagedOrganizationList = MonoUtils.blockOrThrow(mono, GetOrganizationException.class);
		if (pagedOrganizationList != null) {
			final var organizations = pagedOrganizationList.getElements();
			if (CollectionUtils.isNotEmpty(organizations)) {
				return organizations.get(0);
			}
		}
		return null;
	}

	@Nonnull
	public PagedOrganizationList searchOrganizations(UUID uuid, String name, Boolean active, UUID userUuid,OrganizationStatus organizationStatus, Integer offset, Integer limit, String order) throws GetOrganizationException {
		final var mono = organizationWebClient.get()
				.uri(uriBuilder -> uriBuilder.path(organizationProperties.getOrganizationsPath())
						.queryParamIfPresent("uuid", Optional.ofNullable(uuid))
						.queryParamIfPresent("name", Optional.ofNullable(name))
						.queryParamIfPresent("active", Optional.ofNullable(active))
						.queryParamIfPresent("user_uuid", Optional.ofNullable(userUuid))
						.queryParamIfPresent("organization_status", Optional.ofNullable(organizationStatus))
						.queryParamIfPresent("offset", Optional.ofNullable(offset))
						.queryParamIfPresent("limit", Optional.ofNullable(limit))
						.queryParamIfPresent("order", Optional.ofNullable(order))
						.build())
				.retrieve().bodyToMono(PagedOrganizationList.class);
		final var pagedOrganizationList = MonoUtils.blockOrThrow(mono, GetOrganizationException.class);
		if (pagedOrganizationList != null) {
			return pagedOrganizationList;
		}
		return new PagedOrganizationList();
	}

	@Nonnull
	public List<Organization> getAllOrganizations() throws GetOrganizationException {
		List<Organization> organizations = new ArrayList<>();
		int offset = 0;
		long total;
		do {
			PagedOrganizationList organizationsPage = searchOrganizations(null, null, null, null, null, offset, 10, "name");
			total = organizationsPage.getTotal();
			if (CollectionUtils.isNotEmpty(organizationsPage.getElements())) {
				organizations.addAll(organizationsPage.getElements());
				offset += organizationsPage.getElements().size();
			}
		} while (organizations.size() < total);
		return organizations;
	}

	public List<UUID> getMyOrganizationsUuids(UUID userUuid) throws GetOrganizationException {
		int limit = 50;
		int offset = 0;

		PagedOrganizationList page = getMyOrganizations(userUuid, offset, limit, null);

		// S'il n'y a aucune organisation, on retourne une liste vide
		if (page == null || page.getTotal() <= 0) {
			return new ArrayList<>();
		}

		// Récupération des UUIDs des organisations
		List<UUID> uuids = new ArrayList<>();
		uuids.addAll(extractUuidFromPageList(page));

		// S'il y a plus de limit organization lié au user, on itère sur la requête pour récupérer toutes les organizations
		if(page.getTotal() > limit) {
			offset += limit;
			while(page.getTotal() > offset) {
				page = getMyOrganizations(userUuid, offset, limit, null);
				uuids.addAll(extractUuidFromPageList(page));
				offset += limit;
			}
		}

		return uuids;
	}

	public PagedOrganizationList getMyOrganizations(UUID userUuid, int offset, int limit, String order) throws GetOrganizationException {
		return searchOrganizations(null, null, null, userUuid, null, offset, limit, order);
	}

	private List<UUID> extractUuidFromPageList(PagedOrganizationList page) {
		if (page == null || page.getTotal() <= 0) {
			return new ArrayList<>();
		}

		return page.getElements().stream().map(Organization::getUuid).toList();
	}
}
