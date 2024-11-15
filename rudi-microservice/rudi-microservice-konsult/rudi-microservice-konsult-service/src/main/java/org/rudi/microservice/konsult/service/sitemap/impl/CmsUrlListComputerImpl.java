package org.rudi.microservice.konsult.service.sitemap.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.cms.CmsService;
import org.rudi.facet.cms.bean.CmsAssetType;
import org.rudi.facet.cms.exception.CmsException;
import org.rudi.facet.cms.impl.model.CmsRequest;
import org.rudi.microservice.konsult.core.sitemap.SitemapDescriptionData;
import org.rudi.microservice.konsult.core.sitemap.SitemapEntryData;
import org.rudi.microservice.konsult.core.sitemap.UrlListTypeData;
import org.rudi.microservice.konsult.service.sitemap.AbstractUrlListComputer;
import org.sitemaps.schemas.sitemap.TUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.rudi.microservice.konsult.service.helper.sitemap.SitemapUtils.normalize;

@Component
@Slf4j
@RequiredArgsConstructor
public class CmsUrlListComputerImpl extends AbstractUrlListComputer {

	private final CmsService cmsService;

	@Getter(AccessLevel.PUBLIC)
	@Value("${front.urlServer}")
	private String urlServer;

	// Constante pour l'ordre
	private static final String CMS_REQUEST_ORDER = "-mgnl:lastModified";


	@Override
	public UrlListTypeData getAcceptedData() {
		return UrlListTypeData.CMS;
	}

	@Override
	public List<TUrl> computeInternal(SitemapEntryData sitemapEntryData, SitemapDescriptionData sitemapDescriptionData) throws AppServiceException {

		// Filtre par défaut
		CmsRequest CMS_REQUEST_DEFAULT_FILTER = CmsRequest.builder().build();

		String formattedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

		// Filtre spécifique pour les "news"
		CmsRequest newsFilter = CmsRequest.builder().filters(List.of("publishdate[lte]=" + formattedDate, "unpublishdate[gt]=" + formattedDate)).build();


		List<String> news = fetchCmsUrls(
				CmsAssetType.NEWS,
				"rudi-news@news-sitemap",
				newsFilter,
				sitemapDescriptionData
		);

		List<String> terms = fetchCmsUrls(
				CmsAssetType.TERMS,
				"rudi-terms@terms-sitemap",
				CMS_REQUEST_DEFAULT_FILTER,
				sitemapDescriptionData
		);

		List<String> projectValues = fetchCmsUrls(
				CmsAssetType.PROJECTVALUES,
				"rudi-project-values@projectvalue-sitemap",
				CMS_REQUEST_DEFAULT_FILTER,
				sitemapDescriptionData
		);

		return Stream.of(news, terms, projectValues)
				.flatMap(list -> convertToTUrlList(list).stream())
				.collect(Collectors.toList());
	}


	/**
	 * Récupère les URLs des assets CMS pour un type d'asset spécifique en fonction des paramètres fournis.
	 *
	 * @param assetType Le type d'asset CMS à récupérer (ex. NEWS, TERMS, PROJECTVALUES).
	 * @param sitemap Le nom du template de sitemap pour structurer les URLs générées.
	 * @param request L'objet `CmsRequest` contenant les filtres et autres paramètres de la requête.
	 * @param sitemapDescriptionData Les données de description du sitemap, contenant notamment le nombre maximal d'URLs à récupérer.
	 * @return Une liste de chaînes de caractères représentant les URLs relatives
	 * 		   (ex: /cms/detail/terms/c108b7f6-d895-4d45-a6f4-251f7f70f62d/rudi-terms@one-term-detailed/Politique_de_confidentialit%C3%A9_de_la_plateforme_Rudi)
	 * 		   des assets CMS, ordonnées par date de dernière modification.
	 *         Renvoie une liste vide en cas d'échec ou d'exception.
	 */
	private List<String> fetchCmsUrls(CmsAssetType assetType, String sitemap, CmsRequest request, SitemapDescriptionData sitemapDescriptionData) {
		try {
			return cmsService.renderAssetsAsUrl(
					assetType,
					sitemap,
					request,
					0,
					sitemapDescriptionData.getMaxUrlCount(),
					CMS_REQUEST_ORDER
			);
		} catch (CmsException exception) {
			log.error("Erreur lors de la récupération des assets url de type {} pour le sitemap", assetType, exception);
			return Collections.emptyList(); // Return an empty list on error
		}
	}

	/**
	 * Convertit une liste de chaînes de caractères en une liste d'objets `TUrl`.
	 *
	 * @param elements La liste de chaînes de caractères représentant les URLs à convertir en objets `TUrl`.
	 * @return Une liste d'objets `TUrl` où chaque élément de la liste `elements` est transformé en un objet `TUrl`.
	 **/
	private List<TUrl> convertToTUrlList(List<String> elements) {
		List<TUrl> urlList = new ArrayList<>();
		elements.forEach(element -> {
			TUrl url = new TUrl();
			url.setLoc(buildLocation(element));
			urlList.add(url);
		});
		return urlList;
	}


	/**
	 * Construit une URL en décodant la dernière partie du lien fourni pour la rendre compatible, puis en la reconstruisant avec une urlServer de base.
	 *
	 * @param link L'URL d'entrée sous forme de chaîne de caractères.
	 *             Cette URL doit contenir une structure avec des barres obliques ("/") pour que la méthode fonctionne correctement.
	 *             (ex:/cms/detail/terms/c108b7f6-d895-4d45-a6f4-251f7f70f62d/rudi-terms@one-term-detailed/Politique_de_confidentialit%C3%A9_de_la_plateforme_Rudi)
	 * @return Une nouvelle URL formée à partir de la base du lien et de la dernière partie décodée.
	 *         La dernière partie est normalisée et encodée selon les normes UTF-8.
	 *         (ex: http://localhost:4200/cms/detail/terms/c108b7f6-d895-4d45-a6f4-251f7f70f62d/rudi-terms@one-term-detailed/politique-de-confidentialite-de-la-plateforme-rudi)
	 */
	private String buildLocation(String link) {
		if (link.startsWith("/")) {
			link = link.substring(1);
		}
			int lastSlashIndex = link.lastIndexOf('/');
			String baseUrl = link.substring(0, lastSlashIndex + 1);
			String lastPart = link.substring(lastSlashIndex + 1);
			String encodedLastPart = normalize(URLDecoder.decode(lastPart, StandardCharsets.UTF_8));
			return StringUtils.join(urlServer, (baseUrl + encodedLastPart));
	}
}
