package org.rudi.facet.selfdata.helper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.workflow.SelfdataTaskSearchCriteria;
import bean.workflow.Task;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SelfdataHelper {

	@Autowired
	@Qualifier("selfdataWebClient")
	private WebClient selfdataWebClient;

	private final SelfdataProperties selfdataProperties;

	/**
	 * On considère qu'un utilisateur est autorisé à accéder à un JDD Selfdata si sa demande d'accès sur ce JDD a été validé par l'animateur et le statut
	 * de sa demande est TraitéPrésent
	 *
	 * @param userUuid    UUID de l'utilisateur
	 * @param datasetUuid UUID du jeu de données
	 * @return true si l'utilisateur conecté à accès au jeu de données
	 */
	public boolean hasMatchingToDataset(UUID userUuid, UUID datasetUuid) {
		Boolean userHasAccessToDataset = hasMonoMatchingToDataset(userUuid, datasetUuid).block();
		return BooleanUtils.isTrue(userHasAccessToDataset);
	}

	public Mono<Boolean> hasMonoMatchingToDataset(UUID userUuid, UUID datasetUuid) {
		return selfdataWebClient.get().uri(uriBuilder -> uriBuilder
				.path(selfdataProperties.getHasMatchingToDatasetPath()).build(userUuid, datasetUuid)).retrieve()
				.bodyToMono(Boolean.class);
	}

	public Mono<UUID> getMatchingToken(UUID datasetUuid, String login) {
		return selfdataWebClient.get()
				.uri(uriBuilder -> uriBuilder.path(selfdataProperties.getMatchingTokenPath()).build(datasetUuid, login))
				.retrieve().bodyToMono(UUID.class);
	}

	public List<Task> searchTasks(SelfdataTaskSearchCriteria taskSearchCriteria) {
		return selfdataWebClient.get().uri(uriBuilder -> uriBuilder.path(selfdataProperties.getSearchTasksPath())
						.queryParamIfPresent("title", Optional.ofNullable(taskSearchCriteria.getTitle()))
						.queryParamIfPresent("description", Optional.ofNullable(taskSearchCriteria.getDescription()))
						.queryParamIfPresent("processDefinitionKeys", Optional.ofNullable(taskSearchCriteria.getProcessDefinitionKeys()))
						.queryParamIfPresent("status", Optional.ofNullable(taskSearchCriteria.getStatus()))
						.queryParamIfPresent("functionalStatus", Optional.ofNullable(taskSearchCriteria.getFunctionalStatus()))
						.queryParamIfPresent("asAdmin", Optional.ofNullable(taskSearchCriteria.getAsAdmin()))
						.queryParamIfPresent("minCreationDate", Optional.ofNullable(taskSearchCriteria.getMinCreationDate()))
						.queryParamIfPresent("maxCreationDate", Optional.ofNullable(taskSearchCriteria.getMaxCreationDate()))
						.queryParamIfPresent("userLogin", Optional.ofNullable(taskSearchCriteria.getUserLogin()))
						.build()).retrieve()
				.bodyToMono(Task[].class)
				.blockOptional()
				.map(Arrays::asList)
				.orElse(List.of());
	}

	public void notifyUserDeactivation(String login, UUID userUuid) {
		selfdataWebClient.put()
				.uri(uriBuilder -> uriBuilder.path(selfdataProperties.getHandleUserDeactivationPath())
						.queryParamIfPresent("login", Optional.ofNullable(login))
						.queryParamIfPresent("userUuid", Optional.ofNullable(userUuid))
						.build())
				.retrieve().bodyToMono(Void.class).block();
	}
}
