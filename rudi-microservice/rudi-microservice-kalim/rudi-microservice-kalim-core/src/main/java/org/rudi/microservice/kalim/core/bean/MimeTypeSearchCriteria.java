package org.rudi.microservice.kalim.core.bean;

import java.util.List;

import org.rudi.common.core.bean.criteria.AbstractStampedSearchCriteria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public class MimeTypeSearchCriteria extends AbstractStampedSearchCriteria {

	List<String> codes;
}
