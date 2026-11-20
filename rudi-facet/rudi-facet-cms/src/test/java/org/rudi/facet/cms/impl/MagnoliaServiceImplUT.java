package org.rudi.facet.cms.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ehcache.Cache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.core.DocumentContent;
import org.rudi.facet.cms.CmsSpringBootTest;
import org.rudi.facet.cms.bean.CmsAsset;
import org.rudi.facet.cms.bean.CmsAssetType;
import org.rudi.facet.cms.bean.CmsCategory;
import org.rudi.facet.cms.bean.PagedCmsAssets;
import org.rudi.facet.cms.datafactory.CmsCategoryDataFactory;
import org.rudi.facet.cms.datafactory.CmsMagnoliaCategoryDataFactory;
import org.rudi.facet.cms.datafactory.CmsMagnoliaPageDataFactory;
import org.rudi.facet.cms.datafactory.CmsMagnoliaTermDataFactory;
import org.rudi.facet.cms.datafactory.CmsRequestDataFactory;
import org.rudi.facet.cms.impl.configuration.CmsMagnoliaConfiguration;
import org.rudi.facet.cms.impl.mapper.CmsCategoryMapperImpl;
import org.rudi.facet.cms.impl.model.CmsMagnoliaCategory;
import org.rudi.facet.cms.impl.model.CmsMagnoliaPage;
import org.rudi.facet.cms.impl.model.CmsMagnoliaTerm;
import org.rudi.facet.cms.impl.model.CmsRequest;
import org.rudi.facet.cms.impl.utils.ResourceUriRewriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@CmsSpringBootTest
class MagnoliaServiceImplUT {

	private static final String FILE_WITH_TAG_FIRST = "inputFiles/selfTagFirstPosition.html";
	private static final String FILE_WITH_TAG_LAST = "inputFiles/selfTagLastPosition.html";
	private static final String FILE_WITH_RESOURCES_IMAGE_BACKGROUND = "inputFiles/resourcesImagesInBackGround.html";
	private static final String FILE_WITH_RESOURCES_IMAGE_IMG = "inputFiles/resourcesImagesInImageBalise.html";
	private static final String FILE_WITH_RESOURCES_FILE_AHREF = "inputFiles/resourcesFileInLink.html";
	private static final String FILE_WITH_ALL_SELF_AND_RESOURCES = "inputFiles/completeExemple.html";

	private MagnoliaServiceImpl magnoliaService;

	@Mock
	private CmsMagnoliaTermsHandler termsHandler;

	@Mock
	private CmsMagnoliaConfiguration cmsMagnoliaConfiguration;

	@Mock
	private Cache<String, CmsCategory> cacheCategories;

	@Mock
	private Cache<String, CmsAsset> cacheAssets;

	@Mock
	private Cache<String, DocumentContent> cacheResources;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private WebClient magnoliaWebClient;

	@Autowired
	private List<ResourceUriRewriter> rewriters;

	@Autowired
	private CmsCategoryDataFactory cmsCategoryDataFactory;

	@Autowired
	private CmsMagnoliaCategoryDataFactory cmsMagnoliaCategoryDataFactory;

	@Autowired
	private CmsMagnoliaTermDataFactory cmsMagnoliaTermDataFactory;

	@Autowired
	private CmsMagnoliaPageDataFactory cmsMagnoliaPageDataFactory;

	@Autowired
	private CmsRequestDataFactory cmsRequestDataFactory;

	@InjectMocks
	private CmsCategoryMapperImpl cmsCategoryMapper;

	@BeforeEach
	void setUp() {
		magnoliaService = new MagnoliaServiceImpl(cmsMagnoliaConfiguration, Arrays.asList(termsHandler),
				cmsCategoryMapper, rewriters);

		ReflectionTestUtils.setField(magnoliaService, "magnoliaWebClient", magnoliaWebClient);
		ReflectionTestUtils.setField(magnoliaService, "cacheCategories", cacheCategories);
		ReflectionTestUtils.setField(magnoliaService, "cacheAssets", cacheAssets);
		ReflectionTestUtils.setField(magnoliaService, "cacheResources", cacheResources);

		lenient().when(cmsMagnoliaConfiguration.getFrontOfficeRoute()).thenReturn("/default/value/self");
		lenient().when(cmsMagnoliaConfiguration.getFrontOfficeResourcesRoute()).thenReturn("/default/value/resources");
		lenient().when(termsHandler.getAssetType()).thenReturn(CmsAssetType.TERMS);
		lenient().when(cmsMagnoliaConfiguration.getCssSelector(CmsAssetType.TERMS)).thenReturn("terms-container");
		lenient().when(cmsMagnoliaConfiguration.getTemporaryDirectory())
				.thenReturn(System.getProperty("java.io.tmpdir"));
		lenient().when(cmsMagnoliaConfiguration.getTemporaryFileExtension()).thenReturn(".html");
		lenient().when(cmsMagnoliaConfiguration.getTemporaryFilePrefix()).thenReturn("rudi-cms-test-");
		lenient().when(cmsMagnoliaConfiguration.getAssetTypeTitleTemplates())
				.thenReturn(Map.of("terms", "terms-title-template"));
	}

	private Element getElement(String fileToParse, String baseUri, String cssQuery)
			throws IOException, URISyntaxException {
		Path path = Paths.get(getClass().getClassLoader().getResource(fileToParse).toURI());

		Stream<String> lines = Files.lines(path);
		String data = lines.collect(Collectors.joining("\n"));
		lines.close();

		Document document = Jsoup.parse(data, baseUri);

		return document.selectFirst(cssQuery);
	}

	@Test
	void replaceSelfLinksFirstPosition() throws IOException, URISyntaxException {
		Element element = getElement(MagnoliaServiceImplUT.FILE_WITH_TAG_FIRST, "UTF-8", "div." + "terms-container");
		Element elementUnchanged = element.clone();

		assertThat(element.select(MagnoliaServiceImpl.SELF_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien au moins un self").isFalse();

		Element modifiedElement = magnoliaService.replaceSelfLinks(element);

		assertThat(modifiedElement).as("Le résultat doit être différent de l'input").isNotEqualTo(elementUnchanged);
		assertThat(modifiedElement.select(MagnoliaServiceImpl.SELF_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir de @self").isTrue();
	}

	@Test
	void replaceSelfLinksLastPosition() throws IOException, URISyntaxException {
		Element element = getElement(MagnoliaServiceImplUT.FILE_WITH_TAG_LAST, "UTF-8", "div." + "terms-container");
		Element elementUnchanged = element.clone();

		assertThat(element.select(MagnoliaServiceImpl.SELF_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien au moins un self").isFalse();

		Element modifiedElement = magnoliaService.replaceSelfLinks(element);

		assertThat(modifiedElement).as("Le résultat doit être différent de l'input").isNotEqualTo(elementUnchanged);
		assertThat(modifiedElement.select(MagnoliaServiceImpl.SELF_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir de @self").isTrue();
	}

	@Test
	void replaceImageLinkInBackground() throws IOException, URISyntaxException {
		Element element = getElement(MagnoliaServiceImplUT.FILE_WITH_RESOURCES_IMAGE_BACKGROUND, "UTF-8",
				"div." + "terms-container");
		Element elementUnchanged = element.clone();

		assertThat(element.select(MagnoliaServiceImpl.STYLE_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien une image en background").isFalse();

		Element modifiedElement = magnoliaService.replaceResourcesLinks(element);

		assertThat(modifiedElement).as("Le resultat doit être different de l'input").isNotEqualTo(elementUnchanged);
		assertThat(modifiedElement.select(MagnoliaServiceImpl.STYLE_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir le même lien dans le background").isTrue();
	}

	@Test
	void replaceImageLinkInImg() throws IOException, URISyntaxException {
		Element element = getElement(MagnoliaServiceImplUT.FILE_WITH_RESOURCES_IMAGE_IMG, "UTF-8",
				"div." + "terms-container");
		Element elementUnchanged = element.clone();

		assertThat(element.select(MagnoliaServiceImpl.IMAGE1_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien une image dans une balise img").isFalse();

		Element modifiedElement = magnoliaService.replaceResourcesLinks(element);

		assertThat(modifiedElement).as("Le resultat doit être different de l'input").isNotEqualTo(elementUnchanged);
		assertThat(modifiedElement.select(MagnoliaServiceImpl.IMAGE1_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir le même lien dans la balise").isTrue();
	}

	@Test
	void replaceResourceHref() throws IOException, URISyntaxException {
		Element element = getElement(MagnoliaServiceImplUT.FILE_WITH_RESOURCES_FILE_AHREF, "UTF-8",
				"div." + "terms-container");
		Element elementUnchanged = element.clone();

		assertThat(element.select(MagnoliaServiceImpl.ANCHOR_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien une ressource dans un href").isFalse();

		Element modifiedElement = magnoliaService.replaceResourcesLinks(element);

		assertThat(modifiedElement).as("Le resultat doit être different de l'input").isNotEqualTo(elementUnchanged);
		assertThat(modifiedElement.select(MagnoliaServiceImpl.ANCHOR_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir le même lien dans le href de la balise a").isTrue();
	}

	@Test
	void replaceSelfImageAndResource() throws IOException, URISyntaxException {
		Element element = getElement(MagnoliaServiceImplUT.FILE_WITH_ALL_SELF_AND_RESOURCES, "UTF-8",
				"div." + "news-container");
		Element elementUnchanged = element.clone();

		assertThat(element.select(MagnoliaServiceImpl.SELF_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien au moins un self").isFalse();
		assertThat(element.select(MagnoliaServiceImpl.STYLE_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien une image en background").isFalse();
		assertThat(element.select(MagnoliaServiceImpl.IMAGE1_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien une image dans une balise img").isFalse();
		assertThat(element.select(MagnoliaServiceImpl.ANCHOR_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien une ressource dans un href").isFalse();
		assertThat(element.select(MagnoliaServiceImpl.LINK_CSS_QUERY).isEmpty())
				.as("Le fichier contient bel et bien un link").isFalse();

		magnoliaService.replaceSelfLinks(element);
		magnoliaService.replaceResourcesLinks(element);

		assertThat(element).as("Le résultat doit être différent de l'input").isNotEqualTo(elementUnchanged);
		assertThat(element.select(MagnoliaServiceImpl.SELF_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir de @self").isTrue();
		assertThat(element.select(MagnoliaServiceImpl.STYLE_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir le même lien dans le background").isTrue();
		assertThat(element.select(MagnoliaServiceImpl.IMAGE1_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir le même lien dans la balise").isTrue();
		assertThat(element.select(MagnoliaServiceImpl.ANCHOR_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir le même lien dans le href de la balise a").isTrue();
		assertThat(element.select(MagnoliaServiceImpl.LINK_CSS_QUERY).isEmpty())
				.as("Le résultat ne doit plus contenir le même link").isTrue();
	}

	@Test
	@DisplayName("Récupération de toutes les catégories : la liste est mappée et mise en cache")
	void test_getCategories_nominal() throws Exception {
		CmsMagnoliaCategory categoryNode1 = cmsMagnoliaCategoryDataFactory.createCmsMagnoliaCategory("terms-cat-1",
				"/rudi/terms", "Terms label");
		CmsMagnoliaCategory categoryNode2 = cmsMagnoliaCategoryDataFactory.createCmsMagnoliaCategory("terms-cat-2",
				"/rudi/news", "News label");
		CmsMagnoliaPage<CmsMagnoliaCategory> page = cmsMagnoliaPageDataFactory.createCmsMagnoliaPage(2,
				List.of(categoryNode1, categoryNode2));
		when(magnoliaWebClient.get().uri(anyString(), any(Function.class)).retrieve()
				.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(page));

		List<CmsCategory> result = magnoliaService.getCategories();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getPath()).isEqualTo("/rudi/terms");
		assertThat(result.get(0).getLabel()).isEqualTo("Terms label");
		assertThat(result.get(1).getPath()).isEqualTo("/rudi/news");
		assertThat(result.get(1).getLabel()).isEqualTo("News label");
		verify(cacheCategories).put(eq("/rudi/terms"), any());
		verify(cacheCategories).put(eq("/rudi/news"), any());
	}

	@Test
	@DisplayName("Récupération d'une catégorie par son path absente du cache : rechargement via les catégories")
	void test_getCategory_nominal() throws Exception {
		String path = "/rudi/terms";
		CmsCategory expectedCategory = cmsCategoryDataFactory.createCmsCategory("terms-cat-1", path, "Terms label");
		when(cacheCategories.get(path)).thenReturn(null, expectedCategory);
		CmsMagnoliaCategory categoryNode = cmsMagnoliaCategoryDataFactory.createCmsMagnoliaCategory("terms-cat-1",
				path, "Terms label");
		CmsMagnoliaPage<CmsMagnoliaCategory> page = cmsMagnoliaPageDataFactory.createCmsMagnoliaPage(1,
				List.of(categoryNode));
		when(magnoliaWebClient.get().uri(anyString(), any(Function.class)).retrieve()
				.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(page));

		CmsCategory result = magnoliaService.getCategory(path);

		assertThat(result.getId()).isEqualTo("terms-cat-1");
		assertThat(result.getPath()).isEqualTo(path);
		assertThat(result.getLabel()).isEqualTo("Terms label");
		verify(cacheCategories).put(eq(path), any());
	}

	@Test
	@DisplayName("Rendition d'un asset absent du cache : téléchargement du contenu et calcul du titre")
	void test_renderAsset_nominal() throws Exception {
		String assetId = "term-1";
		String assetTemplate = "terms-template";
		CmsMagnoliaTerm node = cmsMagnoliaTermDataFactory.createCmsMagnoliaTerm(assetId);
		when(termsHandler.findItem(assetId)).thenReturn(node);
		DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		DataBuffer contentBuffer = bufferFactory.wrap(
				"<div class=\"terms-container\"><a href=\"@self/terms/term-1/terms-template/titre\">Lien</a></div>"
						.getBytes(StandardCharsets.UTF_8));
		DataBuffer titleBuffer = bufferFactory.wrap("<p>Titre du terme</p>".getBytes(StandardCharsets.UTF_8));
		when(magnoliaWebClient.get().uri(anyString(), any(Function.class)).retrieve().bodyToFlux(DataBuffer.class))
				.thenReturn(Flux.just(contentBuffer), Flux.just(titleBuffer));

		CmsAsset result = magnoliaService.renderAsset(CmsAssetType.TERMS, assetId, assetTemplate, Locale.FRENCH);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(assetId);
		assertThat(result.getTitle()).isEqualTo("Titre du terme");
		assertThat(result.getContent()).contains("/default/value/self/terms/term-1/terms-template/titre")
				.doesNotContain("@self");
		verify(cacheAssets).put(eq("TERMS@" + assetId + "@" + assetTemplate), any());
	}

	@Test
	@DisplayName("Rendition de tous les assets d'une catégorie : agrégation et report des dates de l'item")
	void test_renderAssets_nominal() throws Exception {
		String categoryPath = "/rudi/terms";
		CmsCategory category = cmsCategoryDataFactory.createCmsCategory("terms-cat-1", categoryPath);
		when(cacheCategories.get(categoryPath)).thenReturn(category);

		CmsMagnoliaTerm item = cmsMagnoliaTermDataFactory.createCmsMagnoliaTerm("term-1",
				LocalDateTime.of(2024, 1, 1, 10, 0), LocalDateTime.of(2024, 2, 1, 10, 0));
		CmsMagnoliaPage<CmsMagnoliaTerm> page = cmsMagnoliaPageDataFactory.createCmsMagnoliaPage(1, List.of(item));
		CmsRequest request = cmsRequestDataFactory.createCmsRequestWithCategories(List.of(categoryPath));
		when(termsHandler.searchItems(request, 0, 10, "-creationDate")).thenReturn(page);

		DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		DataBuffer contentBuffer = bufferFactory
				.wrap("<div class=\"terms-container\">Contenu</div>".getBytes(StandardCharsets.UTF_8));
		DataBuffer titleBuffer = bufferFactory.wrap("<p>Titre du terme</p>".getBytes(StandardCharsets.UTF_8));
		when(magnoliaWebClient.get().uri(anyString(), any(Function.class)).retrieve().bodyToFlux(DataBuffer.class))
				.thenReturn(Flux.just(contentBuffer), Flux.just(titleBuffer));

		PagedCmsAssets result = magnoliaService.renderAssets(CmsAssetType.TERMS, "terms-template", request, 0, 10,
				"-creationDate");

		assertThat(result.getTotal()).isEqualTo(1);
		assertThat(result.getElements()).hasSize(1);
		assertThat(result.getElements().get(0).getId()).isEqualTo("term-1");
		assertThat(result.getElements().get(0).getCreationDate()).isEqualTo(item.getCreated());
		assertThat(result.getElements().get(0).getUpdateDate()).isEqualTo(item.getLastModified());
	}

	@Test
	@DisplayName("Rendition des assets sous forme d'URL : extraction du premier lien du contenu")
	void test_renderAssetsAsUrl_nominal() throws Exception {
		String categoryPath = "/rudi/news";
		CmsCategory category = cmsCategoryDataFactory.createCmsCategory("news-cat-1", categoryPath);
		when(cacheCategories.get(categoryPath)).thenReturn(category);

		CmsMagnoliaTerm item = cmsMagnoliaTermDataFactory.createCmsMagnoliaTerm("term-2");
		CmsMagnoliaPage<CmsMagnoliaTerm> page = cmsMagnoliaPageDataFactory.createCmsMagnoliaPage(1, List.of(item));
		CmsRequest request = cmsRequestDataFactory.createCmsRequestWithCategories(List.of(categoryPath));
		when(termsHandler.searchItems(request, 0, 10, null)).thenReturn(page);

		DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		DataBuffer contentBuffer = bufferFactory.wrap(
				"<div class=\"terms-container\"><a href=\"https://example.org/news/term-2\">Lien</a></div>"
						.getBytes(StandardCharsets.UTF_8));
		DataBuffer titleBuffer = bufferFactory.wrap("<p>Titre</p>".getBytes(StandardCharsets.UTF_8));
		when(magnoliaWebClient.get().uri(anyString(), any(Function.class)).retrieve().bodyToFlux(DataBuffer.class))
				.thenReturn(Flux.just(contentBuffer), Flux.just(titleBuffer));

		List<String> result = magnoliaService.renderAssetsAsUrl(CmsAssetType.TERMS, "terms-template", request, 0, 10,
				null);

		assertThat(result).containsExactly("https://example.org/news/term-2");
	}

	@Test
	@DisplayName("Téléchargement d'une ressource absente du cache : écriture en fichier et détection du type MIME")
	void test_downloadResource_nominal() throws Exception {
		String resourcePath = "/dam/jcr:resource/document.txt";
		DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		DataBuffer buffer = bufferFactory.wrap("Contenu de test".getBytes(StandardCharsets.UTF_8));
		when(magnoliaWebClient.get().uri(resourcePath).retrieve().bodyToFlux(DataBuffer.class))
				.thenReturn(Flux.just(buffer));

		DocumentContent result = magnoliaService.downloadResource(resourcePath);

		assertThat(result).isNotNull();
		assertThat(result.isFile()).isTrue();
		assertThat(result.getContentType()).isNotBlank();
		verify(cacheResources).put(eq(resourcePath), eq(result));
	}

}
