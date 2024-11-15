package org.rudi.tools.nodestub.service;

import java.io.IOException;

import org.rudi.common.core.DocumentContent;
import org.springframework.stereotype.Service;

@Service
public interface MediaService {
	/**
	 *
	 * @param name resourceName + extension
	 * @param crypted if the resource should be crypted
	 * @return the resource
	 */
	DocumentContent getResourceByName(String name, boolean crypted) throws IOException;
}
