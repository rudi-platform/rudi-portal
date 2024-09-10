package org.rudi.common.core.bean.criteria;

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
public abstract class AbstractStampedSearchCriteria extends AbstractLabelizedSearchCriteria{
	private Boolean active;
}
