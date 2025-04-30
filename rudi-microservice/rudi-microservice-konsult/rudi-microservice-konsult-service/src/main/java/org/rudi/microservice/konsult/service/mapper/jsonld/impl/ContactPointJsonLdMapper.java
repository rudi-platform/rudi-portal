package org.rudi.microservice.konsult.service.mapper.jsonld.impl;

import com.google.gson.JsonObject;
import io.micrometer.common.util.StringUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.kaccess.bean.Contact;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.AbstractKindJsonLdMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContactPointJsonLdMapper extends AbstractKindJsonLdMapper<Contact> {

	public ContactPointJsonLdMapper(MediaUrlHelper mediaUrlHelper) {
		super(mediaUrlHelper);
	}


	@Override
	public JsonObject toJsonLd(Contact contact, DcatJsonLdContext context) throws AppServiceException {
		return toJsonLd(contact.getContactId(), extractValue(contact), contact.getEmail(), null);
	}


	protected String extractValue(Contact contact) {
		if(StringUtils.isNotEmpty(contact.getOrganizationName())) {
			return contact.getOrganizationName();
		}

		if(StringUtils.isNotEmpty(contact.getContactName())) {
			return contact.getContactName();
		}

		if(StringUtils.isNotEmpty(contact.getEmail())) {
			return contact.getEmail();
		}

		return "Contact";
	}
}
