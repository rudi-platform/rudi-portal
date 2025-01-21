/**
 * RUDI
 */
package org.rudi.facet.bpmn.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.SimpleScriptContext;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.scripting.Resolver;
import org.activiti.engine.impl.scripting.ScriptBindings;

/**
 * @author FNI18300
 *
 */
public class NScriptBindings extends ScriptBindings {

	private static final String CONTEXT_VAR_NAME = "context";

	private static final String EXECUTION_VAR_NAME = "execution";

	private ProcessEngineConfigurationImpl processEngineConfiguration;

	public NScriptBindings(ProcessEngineConfigurationImpl processEngineConfiguration, List<Resolver> scriptResolvers,
			VariableScope variableScope, boolean storeScriptVariables) {
		super(scriptResolvers, variableScope, storeScriptVariables);
		this.processEngineConfiguration = processEngineConfiguration;
	}

	public NScriptBindings(ProcessEngineConfigurationImpl processEngineConfiguration, List<Resolver> scriptResolvers,
			VariableScope variableScope) {
		super(scriptResolvers, variableScope);
		this.processEngineConfiguration = processEngineConfiguration;
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		// Cette méthode n'était pas utilisé par l'engine js nashorn ou juel
		// Maintenant elle est utilisée par GraalJS pour envoyer TOUTES les variables
		// connues dans les JS
		Map<String, Object> map = new HashMap<>();
		// on ajoute tous les beans utile (donc les XXXWorfklowContext
		for (Map.Entry<Object, Object> beanEntry : processEngineConfiguration.getBeans().entrySet()) {
			if (!beanEntry.getKey().toString().startsWith("polyglot")) {
				map.put(beanEntry.getKey().toString(), beanEntry.getValue());
			}
		}

		// on ajoute tous ceux qui sont en variables "standard"
		for (Map.Entry<String, Object> entry : super.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}

		// on ajoute l'objet courant (le variable scope
		map.computeIfAbsent(EXECUTION_VAR_NAME, k -> variableScope);
		// on pseudo context de script
		map.computeIfAbsent(CONTEXT_VAR_NAME, k -> new SimpleScriptContext());
		// tous les default bindings
		if (!defaultBindings.isEmpty()) {
			map.putAll(defaultBindings);
		}
		return map.entrySet();
	}

}
