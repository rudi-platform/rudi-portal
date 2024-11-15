package org.rudi.tools.nodestub.service.impl;

import java.io.IOException;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.tools.nodestub.service.MediaService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

	private final ResourceHelper resourceHelper;
	/**
	 * @param name    resourceName + extension
	 * @param crypted if the resource should be crypted
	 * @return the resource
	 */
	@Override
	public DocumentContent getResourceByName(String name, boolean crypted) throws IOException {
		return resourceHelper.convertToDocumentContent(resourceHelper.getResourceFromAdditionalLocationOrFromClasspath(name));
	}
}
