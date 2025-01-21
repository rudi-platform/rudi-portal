/**
 * RUDI Portail
 */
package org.rudi.facet.bpmn.helper.workflow;

import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.dao.workflow.AssetDescription2TestDao;
import org.rudi.facet.bpmn.entity.workflow.AssetDescription2TestEntity;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.springframework.stereotype.Component;

/**
 * @author FNI18300
 *
 */
@Component(value = "test2WorkflowContext")
public class Test2WorkflowContext
		extends AbstractWorkflowContext<AssetDescription2TestEntity, AssetDescription2TestDao, Assigment2TestHelper> {

	public Test2WorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			AssetDescription2TestDao assetDescriptionDao, Assigment2TestHelper assignmentHelper, ACLHelper aclHelper,
			FormHelper formHelper) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
	}

	public String getType() {
		return "accept";
	}
}
