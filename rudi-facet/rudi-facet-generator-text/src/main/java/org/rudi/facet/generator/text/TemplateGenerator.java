/**
 * RUDI Portail
 */
package org.rudi.facet.generator.text;

import java.io.IOException;

import org.rudi.common.core.DocumentContent;
import org.rudi.facet.generator.exception.GenerationException;
import org.rudi.facet.generator.exception.GenerationModelNotFoundException;
import org.rudi.facet.generator.model.DocumentDataModel;

/**
 * FNI18300
 */
public interface TemplateGenerator {

	DocumentContent generateDocument(DocumentDataModel dataModel)
			throws GenerationModelNotFoundException, GenerationException, IOException;

}
