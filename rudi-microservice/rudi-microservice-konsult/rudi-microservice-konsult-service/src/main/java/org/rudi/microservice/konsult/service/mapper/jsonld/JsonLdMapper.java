package org.rudi.microservice.konsult.service.mapper.jsonld;

import com.google.gson.JsonObject;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;

public interface JsonLdMapper<T> {


	public JsonObject toJsonLd(T item, DcatJsonLdContext context) throws AppServiceException;

}
