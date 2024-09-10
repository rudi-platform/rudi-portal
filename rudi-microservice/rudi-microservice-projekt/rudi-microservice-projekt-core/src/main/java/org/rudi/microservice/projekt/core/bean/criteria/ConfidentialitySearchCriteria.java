package org.rudi.microservice.projekt.core.bean.criteria;

import org.rudi.common.core.bean.criteria.AbstractStampedSearchCriteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class ConfidentialitySearchCriteria extends AbstractStampedSearchCriteria {
	private Boolean isPrivate;
}
