package org.rudi.facet.dataverse.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DataverseResponse<T extends Serializable> extends ApiResponseInfo {

	private static final long serialVersionUID = 710847559178534388L;

	private T data;
}
