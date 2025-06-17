package org.rudi.common.core.bean.criteria;

import java.util.UUID;

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
public abstract class AbstractLongIdSearchCriteria implements SearchCriteria {
	private Long id;
	private UUID uuid;
}
