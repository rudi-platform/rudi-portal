package org.rudi.facet.organization.helper.exceptions;

import org.rudi.facet.strukture.exceptions.StruktureApiException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@SuppressWarnings("java:S110") // trop de parents selon Sonar, mais ils sont nécessaires
public class GetOrganizationMembersException extends StruktureApiException {

	private static final long serialVersionUID = 7577925082695869513L;

	public GetOrganizationMembersException(WebClientResponseException cause) {
		super(cause);
	}
}
