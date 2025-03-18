package org.rudi.facet.dataverse.api.dataset;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.DocumentContent;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.dataverse.api.search.AbstractSearchOperationAPI;
import org.rudi.facet.dataverse.bean.DataFile;
import org.rudi.facet.dataverse.bean.Dataset;
import org.rudi.facet.dataverse.bean.DatasetFile;
import org.rudi.facet.dataverse.bean.DatasetVersion;
import org.rudi.facet.dataverse.bean.Identifier;
import org.rudi.facet.dataverse.bean.SearchDatasetInfo;
import org.rudi.facet.dataverse.bean.SearchType;
import org.rudi.facet.dataverse.helper.DataverseWebClientConfig;
import org.rudi.facet.dataverse.helper.RestTemplateHelper;
import org.rudi.facet.dataverse.model.DataverseResponse;
import org.rudi.facet.dataverse.model.search.SearchElements;
import org.rudi.facet.dataverse.model.search.SearchParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

/**
 * Ensemble des opérations sur les datasets, avec l'API du dataverse
 */
@Component
public class DatasetOperationAPI extends AbstractSearchOperationAPI<SearchDatasetInfo> {

	static final String API_DATASETS_PARAM = "datasets";
	static final String API_DATASETS_PERSISTENT_ID_PARAM = ":persistentId";
	private static final String API_DATASETS_VERSIONS_PARAM = "versions";
	private static final String API_DATASETS_DRAFT_PARAM = ":draft";
	private static final String API_DATASETS_MOVE_PARAM = "move";
	private static final String API_DATASETS_DESTROY_PARAM = "destroy";
	private static final String API_DATAVERSES_PARAM = "dataverses";
	static final String PERSISTENT_ID = "persistentId";

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetOperationAPI.class);

	@Value("${temporary.directory:${java.io.tmpdir}}")
	private String temporaryDirectory;

	private RestTemplate restTemplate;

	private RestTemplateHelper restTemplateHelper;

	public DatasetOperationAPI(ObjectMapper defaultObjectMapper, RestTemplateHelper restTemplateHelper) {
		super(defaultObjectMapper);
		this.restTemplateHelper = restTemplateHelper;
	}

	public DocumentContent getSingleDatasetFile(String datasetPersistentId) throws DataverseAPIException {
		DocumentContent documentContent = null;

		// récupérer l'id du fichier à partir du dataset
		final Dataset dataset = getDataset(datasetPersistentId);
		final List<DatasetFile> datasetFiles = dataset.getLatestVersion().getFiles();

		if (CollectionUtils.isNotEmpty(datasetFiles)) {
			// on considère que le dataset n'a qu'un seul fichier
			DataFile dataFile = datasetFiles.get(0).getDataFile();

			if (dataFile != null && dataFile.getId() != null) {

				// charger le fichier
				File file = getDatasetFile(dataFile.getId());
				documentContent = new DocumentContent(dataFile.getFilename(), dataFile.getContentType(),
						dataFile.getFilesize(), file);
			}
		}

		return documentContent;
	}

	public File getDatasetFile(String fileId) throws DataverseAPIException {
		// Ex d'URI : http://dv.open-dev.com:8095/api/access/datafile/581
		String url = createUrl("access", "datafile", fileId);

		LOGGER.debug("Téléchargement du fichier - url : {}", url);
		try {
			ResponseEntity<Flux<DataBuffer>> dataBufferFlux = getWebClient().get().uri(url).retrieve()
					.toEntityFlux(DataBuffer.class).doOnError(t -> handleError("Failed to search dataset", t)).block();
			if (dataBufferFlux == null) {
				throw new IllegalStateException(fileId);
			}
			String fileName = dataBufferFlux.getHeaders().getContentDisposition().getFilename();
			File outputFile = File.createTempFile("rudi", FilenameUtils.getExtension(fileName),
					new File(temporaryDirectory));

			Flux<DataBuffer> body = dataBufferFlux.getBody();
			if (body == null) {
				throw new IllegalStateException(fileId);
			}
			DataBufferUtils.write(body, Path.of(outputFile.getPath()), StandardOpenOption.CREATE)
					.share().block();
			return outputFile;
		} catch (Exception e) {
			throw new DataverseAPIException("Failed to download faile", e);
		}
	}

	/**
	 * @param persistentId l'id du dataset
	 * @return le dataset
	 * @throws DataverseAPIException if Dataset does not exist
	 */
	@Nonnull
	public Dataset getDataset(String persistentId) throws DataverseAPIException {
		String url = createUrl(API_DATASETS_PARAM, API_DATASETS_PERSISTENT_ID_PARAM);
		ParameterizedTypeReference<DataverseResponse<Dataset>> type = new ParameterizedTypeReference<>() {
		};
		DataverseResponse<Dataset> resp = getWebClient().get().uri(uri -> buildGetDataset(uri, url, persistentId))
				.retrieve().bodyToMono(type).doOnError(t -> handleError("Failed to get dataset", t)).block();
		return getDataBody(resp);
	}

	@Override
	public SearchElements<SearchDatasetInfo> searchDataset(SearchParams searchParams) throws DataverseAPIException {
		validateSearchParams(searchParams, SearchType.DATASET);
		return super.searchDataset(searchParams);
	}

	public Identifier createDataset(Dataset dataset, String dataverseIdentifier) throws DataverseAPIException {
		String url = createUrl(API_DATAVERSES_PARAM, dataverseIdentifier, API_DATASETS_PARAM);
		ParameterizedTypeReference<DataverseResponse<Identifier>> type = new ParameterizedTypeReference<>() {
		};
		DataverseResponse<Identifier> resp = getWebClient()
				.post()
				.uri(url)
				.headers(headers -> headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.bodyValue(marshalObject(dataset))
				.retrieve()
				.bodyToMono(type)
				.doOnError(t -> handleError("Failed to create dataset", t))
				.block();
		return getDataBody(resp);
	}

	public DatasetVersion updateDataset(DatasetVersion datasetVersion, String persistentId)
			throws DataverseAPIException {
		String url = createUrl(API_DATASETS_PARAM, API_DATASETS_PERSISTENT_ID_PARAM, API_DATASETS_VERSIONS_PARAM,
				API_DATASETS_DRAFT_PARAM);
		ParameterizedTypeReference<DataverseResponse<DatasetVersion>> type = new ParameterizedTypeReference<>() {
		};

		DataverseResponse<DatasetVersion> resp = getWebClient().put()
				.uri(uri -> buildGetDataset(uri, url, persistentId))
				.headers(headers -> headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.bodyValue(marshalObject(datasetVersion)).retrieve()
				.bodyToMono(type).doOnError(t -> handleError("Failed to update dataset", t)).block();
		return getDataBody(resp);
	}

	public void moveDataset(String persistentId, String dataverseIdentifier) throws DataverseAPIException {
		String url = createUrl(API_DATASETS_PARAM, API_DATASETS_PERSISTENT_ID_PARAM, API_DATASETS_MOVE_PARAM,
				dataverseIdentifier);

		getWebClient().post().uri(uri -> buildGetDataset(uri, url, persistentId)).retrieve().bodyToMono(Void.class)
				.doOnError(t -> handleError("Failed to move dataset", t)).block();
	}

	/**
	 * Suppresion d'un dataset
	 *
	 * @param persistentId l'id du dataset
	 * @throws DataverseAPIException si le dataset n'existe pas
	 */
	public void deleteDataset(String persistentId) throws DataverseAPIException {
		String url = createUrl(API_DATASETS_PARAM, API_DATASETS_PERSISTENT_ID_PARAM, API_DATASETS_DESTROY_PARAM);

		getWebClient().delete().uri(uri -> buildGetDataset(uri, url, persistentId)).retrieve().bodyToMono(Void.class)
				.doOnError(t -> handleError("Failed to delete dataset", t)).block();
	}

	protected URI buildGetDataset(UriBuilder uriBuilder, String url, String persistentId) {
		return uriBuilder.path(url).queryParam(PERSISTENT_ID, persistentId).build();
	}

	/**
	 * @throws org.rudi.facet.dataverse.api.exceptions.DatasetNotFoundException if Dataset does not exist
	 */
	public Dataset getNonBlockingDataset(String persistentId) throws DataverseAPIException {
		String url = restTemplateHelper.getServerUrl()
				+ createUrl(API_DATASETS_PARAM, API_DATASETS_PERSISTENT_ID_PARAM);
		url = UriComponentsBuilder.fromUriString(url).queryParam(PERSISTENT_ID, persistentId).build(true).toUriString();

		HttpEntity<String> entity = createHttpEntity();

		ParameterizedTypeReference<DataverseResponse<Dataset>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<DataverseResponse<Dataset>> dataverseResponse = getRestTemplate().exchange(url, HttpMethod.GET,
				entity, type);
		return dataverseResponse.getBody() != null ? dataverseResponse.getBody().getData() : null;
	}

	/**
	 * Cette méthode est à destination de l'API Gateway qui ne peut pas utiliser le webclient en block
	 *
	 * @param searchParams
	 * @return
	 * @throws DataverseAPIException
	 */
	public SearchElements<SearchDatasetInfo> searchNonBlockingDataset(SearchParams searchParams)
			throws DataverseAPIException {
		validateSearchParams(searchParams, SearchType.DATASET);
		String url = restTemplateHelper.getServerUrl() + createUrl("search");
		url = buildSearchUrl(url, searchParams);
		ParameterizedTypeReference<DataverseResponse<SearchElements<SearchDatasetInfo>>> type = new ParameterizedTypeReference<>() {
		};

		ResponseEntity<DataverseResponse<SearchElements<SearchDatasetInfo>>> dataverseResponse = getRestTemplate()
				.exchange(url, HttpMethod.GET, createHttpEntity(), type);
		return dataverseResponse.getBody() != null ? dataverseResponse.getBody().getData() : null;
	}

	private String buildSearchUrl(String path, SearchParams searchParams) throws DataverseAPIException {

		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(path).queryParam("q", searchParams.getQ());
		EnumSet<SearchType> types = searchParams.getType();
		if (!CollectionUtils.isEmpty(types)) {
			urlBuilder.queryParam("type", types.toArray());
		}
		if (!StringUtils.isEmpty(searchParams.getSubtree())) {
			urlBuilder.queryParam("subtree", searchParams.getSubtree());
		}
		if (!CollectionUtils.isEmpty(searchParams.getFilterQuery())) {
			urlBuilder.queryParam("fq", searchParams.getFilterQuery());
		}
		if (searchParams.getSortBy() != null) {
			urlBuilder.queryParam("sort", searchParams.getSortBy());
		}
		if (searchParams.getSortOrder() != null) {
			urlBuilder.queryParam("order", searchParams.getSortOrder());
		}
		if (searchParams.getPerPage() != null && searchParams.getPerPage() != 0) {
			urlBuilder.queryParam("per_page", searchParams.getPerPage());
		}
		if (searchParams.getStart() != null && searchParams.getStart() != 0) {
			urlBuilder.queryParam("start", searchParams.getStart());
		}
		if (BooleanUtils.isTrue(searchParams.getShowFacets())) {
			urlBuilder.queryParam("show_facets", true);
		}
		if (BooleanUtils.isTrue(searchParams.getShowRelevance())) {
			urlBuilder.queryParam("show_relevance", true);
		}
		if (CollectionUtils.isNotEmpty(searchParams.getMetadatafields())) {
			urlBuilder.queryParam("metadata_fields", searchParams.getMetadatafields());
		}
		try {
			return urlBuilder.build(false).toUriString();
		} catch (IllegalArgumentException e) {
			throw new DataverseAPIException(e);
		}
	}

	private HttpEntity<String> createHttpEntity() {
		HttpHeaders headers = buildHeadersWithApikey();
		return new HttpEntity<>("", headers);
	}

	protected RestTemplate getRestTemplate() throws DataverseAPIException {
		if (restTemplate == null) {
			restTemplate = restTemplateHelper.buildRestTemplate();
			MappingJackson2HttpMessageConverter a = (MappingJackson2HttpMessageConverter) restTemplate
					.getMessageConverters().stream().filter(MappingJackson2HttpMessageConverter.class::isInstance)
					.findFirst().orElse(null);
			if (a != null && !a.getSupportedMediaTypes().contains(MediaType.APPLICATION_OCTET_STREAM)) {
				ArrayList<MediaType> supportedMediaTypes = new ArrayList<>();
				supportedMediaTypes.addAll(a.getSupportedMediaTypes());
				supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);

				a.setSupportedMediaTypes(supportedMediaTypes);
			}
		}
		return restTemplate;
	}

	protected HttpHeaders buildHeadersWithApikey() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.add(DataverseWebClientConfig.API_HEADER_KEY, restTemplateHelper.getApiToken());
		return headers;
	}
}
