/**
 * 
 */
package org.rudi.facet.bpmn.bean.workflow;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author FNI18300
 *
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class HistoricSearchCriteria {

	private boolean asAdmin;

	private List<String> processDefinitionKeys;

	private boolean finished;

	private LocalDateTime minCompletionDate;

	private LocalDateTime maxCompletionDate;

	private List<String> assignees;
}
