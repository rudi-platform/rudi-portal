/**
 * RUDI
 */
package org.rudi.facet.bpmn.config;

import java.util.List;

import javax.script.Bindings;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;

public class NScriptBindingsFactory extends ScriptBindingsFactory {

	public NScriptBindingsFactory(ProcessEngineConfigurationImpl processEngineConfiguration,
			List<ResolverFactory> resolverFactories) {
		super(processEngineConfiguration, resolverFactories);
	}

	@Override
	public Bindings createBindings(VariableScope variableScope) {
		return new NScriptBindings(processEngineConfiguration, createResolvers(variableScope), variableScope);
	}

	@Override
	public Bindings createBindings(VariableScope variableScope, boolean storeScriptVariables) {
		return new NScriptBindings(processEngineConfiguration, createResolvers(variableScope), variableScope,
				storeScriptVariables);
	}

}
