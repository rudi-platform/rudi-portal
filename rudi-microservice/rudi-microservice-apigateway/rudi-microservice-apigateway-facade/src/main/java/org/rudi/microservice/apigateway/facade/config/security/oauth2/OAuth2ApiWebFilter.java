/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.security.oauth2;

import java.util.ArrayList;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.common.facade.gateway.config.oauth2.OAuth2TokenData;
import org.rudi.common.facade.gateway.config.oauth2.OAuth2WebFilter;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.acl.bean.ProjectKeystore;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.ProjectKeystoreSearchCriteria;
import org.rudi.microservice.apigateway.facade.config.gateway.ApiGatewayConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Filtre pour les tokens OAuth2
 * 
 * @author FNI18300
 *
 */
@Slf4j
public class OAuth2ApiWebFilter extends OAuth2WebFilter {

	private ACLHelper aclHelper;

	public OAuth2ApiWebFilter(final String[] excludeUrlPatterns, String checkTokenUri, RestTemplate restTemplate) {
		super(excludeUrlPatterns, checkTokenUri, restTemplate);
	}

	@Override
	protected AuthenticatedUser createAuthenticatedUser(OAuth2TokenData tokenData) {
		AuthenticatedUser user = new AuthenticatedUser(tokenData.getClientId(), UserType.ROBOT);
		ProjectKeystoreSearchCriteria searchCriteria = ProjectKeystoreSearchCriteria.builder()
				.clientId(tokenData.getClientId()).build();
		// on essaye de récupérer le keystore (donc l'uuid du projet)
		Page<ProjectKeystore> keyStores = getAclHelper().searchProjectKeystores(searchCriteria, Pageable.ofSize(1));
		if (!keyStores.isEmpty()) {
			user.addData(ApiGatewayConstants.PROJECTKEY_STORE_UUID,
					keyStores.getContent().getFirst().getProjectUuid().toString());
		}
		user.setRoles(new ArrayList<>());
		if (CollectionUtils.isNotEmpty(tokenData.getAuthorities())) {
			for (String role : tokenData.getAuthorities()) {
				user.getRoles().add(role);
			}
		}
		return user;
	}

	protected ACLHelper getAclHelper() {
		if (aclHelper == null) {
			aclHelper = ApplicationContext.getBean(ACLHelper.class);
		}
		return aclHelper;
	}

}
