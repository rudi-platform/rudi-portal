/**
 * RUDI Portail
 */
package org.rudi.facet.bpmn.helper.workflow;

import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.dao.workflow.AssetDescription1TestDao;
import org.rudi.facet.bpmn.entity.workflow.AssetDescription1TestEntity;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.springframework.stereotype.Component;

/**
 * @author FNI18300
 *
 */
@Component(value = "test1WorkflowContext")
public class Test1WorkflowContext
		extends AbstractWorkflowContext<AssetDescription1TestEntity, AssetDescription1TestDao, Assigment1TestHelper> {

	public Test1WorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			AssetDescription1TestDao assetDescriptionDao, Assigment1TestHelper assignmentHelper, ACLHelper aclHelper,
			FormHelper formHelper) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
	}

	public String getType() {
		return "accept";
	}
}
