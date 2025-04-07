package org.rudi.microservice.konsult.service.sitemap.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.konsult.core.sitemap.SitemapDescriptionData;
import org.rudi.microservice.konsult.core.sitemap.SitemapEntryData;
import org.rudi.microservice.konsult.core.sitemap.UrlListTypeData;
import org.rudi.microservice.konsult.service.sitemap.AbstractUrlListComputer;
import org.rudi.microservice.projekt.core.bean.Project;
import org.rudi.microservice.projekt.core.bean.ProjectSearchCriteria;
import org.sitemaps.schemas.sitemap.TUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.rudi.microservice.konsult.service.helper.sitemap.SitemapUtils.normalize;
import static org.rudi.microservice.projekt.core.bean.ProjectStatus.VALIDATED;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectsUrlListComputerImpl extends AbstractUrlListComputer {

	private final ProjektHelper projektHelper;
	private final UtilPageable utilPageable;

	@Getter(AccessLevel.PUBLIC)
	@Value("${front.urlServer}")
	private String urlServer;

	@Getter(AccessLevel.PUBLIC)
	@Value("/projets/detail/")
	private String catalogueUrlPrefixe;

	@Override
	public UrlListTypeData getAcceptedData() {
		return UrlListTypeData.PROJECTS;
	}

	@Override
	public List<TUrl> computeInternal(SitemapEntryData sitemapEntryData, SitemapDescriptionData sitemapDescriptionData) {

		Page<Project> projectsPage = projektHelper.searchProjects(
				new ProjectSearchCriteria().status(List.of(VALIDATED)),
				utilPageable.getPageable(0, sitemapDescriptionData.getMaxUrlCount(), StringUtils.EMPTY));

		List<TUrl> projectUrlList = new ArrayList<>();
		projectsPage.getContent().forEach(project -> {
			TUrl url = new TUrl();
			url.setLoc(buildLocation(project.getUuid(), project.getTitle()));
			projectUrlList.add(url);
		});

		return projectUrlList;
	}

	private String buildLocation(UUID uuid, String title) {
		return StringUtils.join(
				Arrays.array(StringUtils.removeEnd(urlServer, "/"), catalogueUrlPrefixe, uuid, "/", normalize(title)));
	}
}
