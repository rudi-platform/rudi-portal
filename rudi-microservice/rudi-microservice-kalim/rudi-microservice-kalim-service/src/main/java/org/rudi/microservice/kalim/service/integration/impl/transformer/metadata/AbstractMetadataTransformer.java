package org.rudi.microservice.kalim.service.integration.impl.transformer.metadata;

import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.microservice.kalim.service.integration.impl.transformer.ElementTransformer;

public abstract class AbstractMetadataTransformer<T> implements ElementTransformer<T> {

	protected abstract T getMetadataElementToTransform(Metadata metadata);

	protected abstract void updateMetadata(Metadata metadata, T newValue);

	public void transformMetadata(Metadata metadata) {
		updateMetadata(metadata, this.tranform(getMetadataElementToTransform(metadata)));
	}
}
