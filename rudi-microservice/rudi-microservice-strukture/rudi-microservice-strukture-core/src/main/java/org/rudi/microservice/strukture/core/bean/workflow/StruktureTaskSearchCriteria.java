package org.rudi.microservice.strukture.core.bean.workflow;

import java.util.UUID;

import org.rudi.facet.bpmn.bean.workflow.TaskSearchCriteria;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StruktureTaskSearchCriteria extends TaskSearchCriteria {
	private String name;
	private OrganizationStatus organizationStatus;
	private UUID organizationUuid;

}
