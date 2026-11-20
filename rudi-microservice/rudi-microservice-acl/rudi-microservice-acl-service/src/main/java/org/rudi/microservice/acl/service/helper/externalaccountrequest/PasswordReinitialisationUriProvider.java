package org.rudi.microservice.acl.service.helper.externalaccountrequest;

public interface PasswordReinitialisationUriProvider {

	String getPasswordReinitialisationUri(String authenticatorName);
}
