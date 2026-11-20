package org.rudi.facet.acl.helper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.test.RudiAssertions;
import org.rudi.facet.acl.bean.AddressType;
import org.rudi.facet.acl.bean.EmailAddress;
import org.rudi.facet.acl.bean.ProjectKey;
import org.rudi.facet.acl.bean.ProjectKeystore;
import org.rudi.facet.acl.bean.ProjectKeystorePageResult;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserPageResult;
import org.rudi.facet.acl.datafactory.ProjectKeystoreSearchCriteriaDataFactory;
import org.rudi.facet.acl.datafactory.UserSearchCriteriaDataFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ACLHelperUT {

	private final JsonResourceReader jsonResourceReader = new JsonResourceReader();
	private final UserSearchCriteriaDataFactory userSearchCriteriaDataFactory = new UserSearchCriteriaDataFactory();
	private final ProjectKeystoreSearchCriteriaDataFactory projectKeystoreSearchCriteriaDataFactory = new ProjectKeystoreSearchCriteriaDataFactory();

	@Mock
	private UtilContextHelper utilContextHelper;

	private MockWebServer mockWebServer;
	private ACLHelper aclHelper;

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		aclHelper = new ACLHelper(utilContextHelper);
		final String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
		ReflectionTestUtils.setField(aclHelper, "loadBalancedWebClient", WebClient.builder().baseUrl(baseUrl).build());
		ReflectionTestUtils.setField(aclHelper, "aclServiceURL", "");
		ReflectionTestUtils.setField(aclHelper, "rolesEndpointSearchURL", "/acl/v1/roles");
		ReflectionTestUtils.setField(aclHelper, "usersEndpointSearchURL", "/acl/v1/users");
		ReflectionTestUtils.setField(aclHelper, "userEndpointCRUDURL", "/acl/v1/users");
		ReflectionTestUtils.setField(aclHelper, "userCountEndpointURL", "/acl/v1/users/count");
		ReflectionTestUtils.setField(aclHelper, "updateUserPasswordEndpointUrl", "/acl/v1/users/{login}/password");
		ReflectionTestUtils.setField(aclHelper, "searchOrCreateProjectKeystoresUrl", "/acl/v1/project-keystores");
		ReflectionTestUtils.setField(aclHelper, "getOrDeleteProjectKeystoresUrl",
				"/acl/v1/project-keystores/{project-key-store-uuid}");
		ReflectionTestUtils.setField(aclHelper, "createProjectKeyUrl",
				"/acl/v1/project-keystores/{project-key-store-uuid}/project-keys");
		ReflectionTestUtils.setField(aclHelper, "deleteProjectKeyUrl",
				"/acl/v1/project-keystores/{project-key-store-uuid}/project-keys/{project-key-uuid}");
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	private User buildUser(UUID uuid, String login) {
		return new User().uuid(uuid).login(login);
	}

	private void enqueueJson(Object body) throws JsonProcessingException {
		mockWebServer.enqueue(new MockResponse()
				.setBody(jsonResourceReader.getObjectMapper().writeValueAsString(body))
				.addHeader("Content-Type", "application/json"));
	}

	private void enqueueStatus(int code) {
		mockWebServer.enqueue(new MockResponse().setResponseCode(code));
	}

	@Test
	void test_getUserByUUID_nominal() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final User user = buildUser(uuid, "jdupont");
		enqueueJson(user);

		final User result = aclHelper.getUserByUUID(uuid);

		assertThat(result).isEqualTo(user);
		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.GET).hasPath("/acl/v1/users/" + uuid);
	}

	@Test
	void test_getMonoUserByUUID_nominal() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final User user = buildUser(uuid, "jdupont");
		enqueueJson(user);

		final Mono<User> result = aclHelper.getMonoUserByUUID(uuid);

		StepVerifier.create(result).assertNext(u -> assertThat(u).isEqualTo(user)).verifyComplete();
	}

	@Test
	void test_deleteUserByUUID_nominal() throws Exception {
		final UUID uuid = UUID.randomUUID();
		enqueueStatus(200);

		aclHelper.deleteUserByUUID(uuid);

		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.DELETE).hasPath("/acl/v1/users/" + uuid);
	}

	@Test
	void test_getUserByLogin_nominal() throws Exception {
		final User user = buildUser(UUID.randomUUID(), "jdupont");
		final UserPageResult pageResult = new UserPageResult();
		pageResult.addElementsItem(user);
		enqueueJson(pageResult);

		final User result = aclHelper.getUserByLogin("jdupont");

		assertThat(result).isEqualTo(user);
		final RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getPath()).contains("/acl/v1/users").contains("login=jdupont");
	}

	@Test
	void test_getUserByLoginAndPassword_nominal() throws Exception {
		final User user = buildUser(UUID.randomUUID(), "jdupont");
		final UserPageResult pageResult = new UserPageResult();
		pageResult.addElementsItem(user);
		enqueueJson(pageResult);

		final User result = aclHelper.getUserByLoginAndPassword("jdupont", "secret");

		assertThat(result).isEqualTo(user);
		final RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getPath()).contains("login=jdupont").contains("password=secret");
	}

	@Test
	void test_getMonoUsers_nominal() throws Exception {
		final UUID roleUuid = UUID.randomUUID();
		final User user = buildUser(UUID.randomUUID(), "jdupont");
		final UserPageResult pageResult = new UserPageResult();
		pageResult.setTotal(1L);
		pageResult.addElementsItem(user);
		enqueueJson(pageResult);

		final UserSearchCriteria criteria = userSearchCriteriaDataFactory.createForRoleUuids(List.of(roleUuid));
		final Mono<UserPageResult> result = aclHelper.getMonoUsers(criteria);

		StepVerifier.create(result).assertNext(r -> assertThat(r.getElements()).containsExactly(user))
				.verifyComplete();
		final RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getPath()).contains("role-uuids=" + roleUuid);
	}

	@Test
	void test_createUser_nominal() throws Exception {
		final User user = buildUser(UUID.randomUUID(), "jdupont");
		enqueueJson(user);

		final User result = aclHelper.createUser(user);

		assertThat(result).isEqualTo(user);
		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.POST).hasPath("/acl/v1/users");
	}

	@Test
	void test_updateUser_nominal() throws Exception {
		final User user = buildUser(UUID.randomUUID(), "jdupont");
		enqueueJson(user);

		final User result = aclHelper.updateUser(user);

		assertThat(result).isEqualTo(user);
		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.PUT).hasPath("/acl/v1/users");
	}

	@Test
	void test_searchRoles_nominal() throws Exception {
		final Role role = new Role().uuid(UUID.randomUUID()).code("ADMINISTRATOR");
		enqueueJson(new Role[] { role });

		final List<Role> result = aclHelper.searchRoles();

		assertThat(result).containsExactly(role);
		final RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getPath()).isEqualTo("/acl/v1/roles?active=true");
	}

	@Test
	void test_searchUsers_nominal() throws Exception {
		final Role role = new Role().uuid(UUID.randomUUID()).code("MODERATOR");
		enqueueJson(new Role[] { role });

		final User user = buildUser(UUID.randomUUID(), "jdupont");
		final UserPageResult pageResult = new UserPageResult();
		pageResult.addElementsItem(user);
		enqueueJson(pageResult);

		final List<User> result = aclHelper.searchUsers("MODERATOR");

		assertThat(result).containsExactly(user);
		final RecordedRequest rolesRequest = mockWebServer.takeRequest();
		assertThat(rolesRequest.getPath()).contains("code=MODERATOR");
		final RecordedRequest usersRequest = mockWebServer.takeRequest();
		assertThat(usersRequest.getPath()).contains("role-uuids=" + role.getUuid());
	}

	@Test
	void test_searchUsersWithCriteria_nominal() throws Exception {
		final UUID userUuid = UUID.randomUUID();
		final User user = buildUser(userUuid, "jdupont");
		final UserPageResult pageResult = new UserPageResult();
		pageResult.addElementsItem(user);
		enqueueJson(pageResult);

		final List<User> result = aclHelper.searchUsersWithCriteria(List.of(userUuid), "dupont", "PERSON", 10);

		assertThat(result).containsExactly(user);
		final RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getPath()).contains("user-uuids=" + userUuid).contains("login-and-denomination=dupont")
				.contains("type=PERSON").contains("limit=10");
	}

	@Test
	void test_lookupEMailAddress_nominal() {
		final User user = buildUser(UUID.randomUUID(), "jdupont");
		user.addAddressesItem(new EmailAddress().type(AddressType.EMAIL).email("jdupont@rudi.fr"));

		final String result = aclHelper.lookupEMailAddress(user);

		assertThat(result).isEqualTo("jdupont@rudi.fr");
	}

	@Test
	void test_lookupEmailAddresses_nominal() {
		final User userWithAddress = buildUser(UUID.randomUUID(), "jdupont");
		userWithAddress.addAddressesItem(new EmailAddress().type(AddressType.EMAIL).email("jdupont@rudi.fr"));
		final User userWithLoginAsEmail = buildUser(UUID.randomUUID(), "jmartin@rudi.fr");

		final List<String> result = aclHelper.lookupEmailAddresses(List.of(userWithAddress, userWithLoginAsEmail));

		assertThat(result).containsExactly("jdupont@rudi.fr", "jmartin@rudi.fr");
	}

	@Test
	void test_getAuthenticatedUserUuid_nominal() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final User user = buildUser(uuid, "jdupont");
		final UserPageResult pageResult = new UserPageResult();
		pageResult.addElementsItem(user);
		enqueueJson(pageResult);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(new AuthenticatedUser("jdupont"));

		final UUID result = aclHelper.getAuthenticatedUserUuid();

		assertThat(result).isEqualTo(uuid);
	}

	@Test
	void test_getAuthenticatedUser_nominal() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final User user = buildUser(uuid, "jdupont");
		final UserPageResult pageResult = new UserPageResult();
		pageResult.addElementsItem(user);
		enqueueJson(pageResult);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(new AuthenticatedUser("jdupont"));

		final User result = aclHelper.getAuthenticatedUser();

		assertThat(result).isEqualTo(user);
	}

	@Test
	void test_updateUserPassword_nominal() throws Exception {
		enqueueStatus(200);

		aclHelper.updateUserPassword("jdupont", "oldPwd", "newPwd");

		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.PUT).hasPath("/acl/v1/users/jdupont/password");
		assertThat(request.getBody().readUtf8()).contains("newPwd").contains("oldPwd");
	}

	@Test
	void test_getUserCount_nominal() throws Exception {
		mockWebServer
				.enqueue(new MockResponse().setBody("42").addHeader("Content-Type", "application/json"));

		final Long result = aclHelper.getUserCount();

		assertThat(result).isEqualTo(42L);
	}

	@Test
	void test_createProjectKeyStore_nominal() throws Exception {
		final UUID projectUuid = UUID.randomUUID();
		final ProjectKeystore keystore = new ProjectKeystore();
		keystore.setUuid(UUID.randomUUID());
		keystore.setProjectUuid(projectUuid);
		enqueueJson(keystore);

		final ProjectKeystore result = aclHelper.createProjectKeyStore(projectUuid);

		assertThat(result).isEqualTo(keystore);
		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.POST).hasPath("/acl/v1/project-keystores");
		assertThat(request.getBody().readUtf8()).contains(projectUuid.toString());
	}

	@Test
	void test_getProjectKeyStore_nominal() throws Exception {
		final UUID keystoreUuid = UUID.randomUUID();
		final ProjectKeystore keystore = new ProjectKeystore();
		keystore.setUuid(keystoreUuid);
		enqueueJson(keystore);

		final ProjectKeystore result = aclHelper.getProjectKeyStore(keystoreUuid);

		assertThat(result).isEqualTo(keystore);
		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.GET)
				.hasPath("/acl/v1/project-keystores/" + keystoreUuid);
	}

	@Test
	void test_deleteProjectKeyStore_nominal() throws Exception {
		final UUID keystoreUuid = UUID.randomUUID();
		enqueueStatus(200);

		aclHelper.deleteProjectKeyStore(keystoreUuid);

		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.DELETE)
				.hasPath("/acl/v1/project-keystores/" + keystoreUuid);
	}

	@Test
	void test_createProjectKey_nominal() throws Exception {
		final UUID keystoreUuid = UUID.randomUUID();
		final ProjectKey projectKey = new ProjectKey().uuid(UUID.randomUUID()).name("clientKey");
		enqueueJson(projectKey);

		final ProjectKey result = aclHelper.createProjectKey(keystoreUuid, projectKey);

		assertThat(result).isEqualTo(projectKey);
		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.PUT)
				.hasPath("/acl/v1/project-keystores/" + keystoreUuid + "/project-keys");
	}

	@Test
	void test_deleteProjectKey_nominal() throws Exception {
		final UUID keystoreUuid = UUID.randomUUID();
		final UUID keyUuid = UUID.randomUUID();
		enqueueStatus(200);

		aclHelper.deleteProjectKey(keystoreUuid, keyUuid);

		final RecordedRequest request = mockWebServer.takeRequest();
		RudiAssertions.assertThat(request).hasMethod(HttpMethod.DELETE)
				.hasPath("/acl/v1/project-keystores/" + keystoreUuid + "/project-keys/" + keyUuid);
	}

	@Test
	void test_searchProjectKeystores_nominal() throws Exception {
		final ProjectKeystore keystore = new ProjectKeystore();
		keystore.setUuid(UUID.randomUUID());
		final ProjectKeystorePageResult pageResult = new ProjectKeystorePageResult();
		pageResult.setTotal(1L);
		pageResult.addElementsItem(keystore);
		enqueueJson(pageResult);

		final ProjectKeystoreSearchCriteria criteria = projectKeystoreSearchCriteriaDataFactory
				.createForClientId("client1");
		final Pageable pageable = PageRequest.of(0, 10);

		final Page<ProjectKeystore> result = aclHelper.searchProjectKeystores(criteria, pageable);

		assertThat(result.getContent()).containsExactly(keystore);
		assertThat(result.getTotalElements()).isEqualTo(1L);
		final RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getPath()).contains("client-id=client1");
	}

	@Test
	void test_searchMonoProjectKeystores_nominal() throws Exception {
		final ProjectKeystore keystore = new ProjectKeystore();
		keystore.setUuid(UUID.randomUUID());
		final ProjectKeystorePageResult pageResult = new ProjectKeystorePageResult();
		pageResult.addElementsItem(keystore);
		enqueueJson(pageResult);

		final ProjectKeystoreSearchCriteria criteria = projectKeystoreSearchCriteriaDataFactory
				.createForProjectUuids(List.of(UUID.randomUUID()));
		final Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("creationDate")));

		final Mono<ProjectKeystorePageResult> result = aclHelper.searchMonoProjectKeystores(criteria, pageable);

		StepVerifier.create(result).assertNext(r -> assertThat(r.getElements()).containsExactly(keystore))
				.verifyComplete();
		final RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getPath()).contains("offset=5").contains("limit=5").contains("order=-creationDate")
				.contains("project-uuids=");
	}

}
