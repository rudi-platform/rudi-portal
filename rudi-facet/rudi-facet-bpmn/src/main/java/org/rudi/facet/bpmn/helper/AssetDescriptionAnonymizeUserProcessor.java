package org.rudi.facet.bpmn.helper;

import java.util.UUID;

import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;

public interface AssetDescriptionAnonymizeUserProcessor {

	void process(String login, UUID userUuid) throws FormDefinitionException, InvalidDataException;
}
