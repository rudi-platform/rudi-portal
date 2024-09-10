package org.rudi.microservice.selfdata.service.helper.selfdatadataset;

import java.time.OffsetDateTime;

import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.facet.kaccess.bean.Metadata;

import lombok.Data;

@Data
public class SelfdataApiParameters {
	private Metadata metadata;
	private AuthenticatedUser user;
	private OffsetDateTime minDate;
	private OffsetDateTime maxDate;
}
