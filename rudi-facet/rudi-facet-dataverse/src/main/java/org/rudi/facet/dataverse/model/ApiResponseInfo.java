package org.rudi.facet.dataverse.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiResponseInfo implements Serializable {

	private static final long serialVersionUID = 4458037574977996010L;

	private String status;
	private String message;
}
