/**
 *
 */
package org.rudi.facet.bpmn.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.scripting.BeansResolverFactory;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.scripting.VariableScopeResolverFactory;
import org.activiti.spring.SpringExpressionManager;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author FNI18300
 *
 */
@Configuration
public class ActivitiConfiguration extends AbstractProcessEngineConfigurator {

	@Value("${spring.datasource.url}")
	private String dataSourceURL;

	@Value("${spring.datasource.username}")
	private String dataSourceUsername;

	@Value("${spring.datasource.password}")
	private String dataSourcePassword;

	@Value("${spring.datasource.driver-class-name}")
	private String dataSourceDriver;

	@Value("${rudi.bpmn.schema:}")
	private String dataSourceSchema;

	@Value("${rudi.bpmn.schema.update:true}")
	private String dataSourceSchemaUpdate;

	@Value("${rudi.bpmn.historc.level:full}")
	private String historicLevel;

	@Value("${search.bpmn.polyglot.options:polyglot.js.allowIO,polyglot.js.allowHostClassLookup,polyglot.js.allowHostClassLoading,polyglot.js.allowAllAccess}")
	private List<String> polyglotOptions;

	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration(
			PlatformTransactionManager transactionManager, ApplicationContext applicationContext) {
		SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
		configuration.setTransactionManager(transactionManager);
		configuration.setEventListeners(new ArrayList<>());
		configuration.getEventListeners().add(new HookEventListener());
		configuration.addConfigurator(this);
		configuration.setApplicationContext(applicationContext);
		configuration.setExpressionManager(new SpringExpressionManager(applicationContext, configuration.getBeans()));

		return configuration;
	}

	@Bean
	public ProcessEngine processEngine(ProcessEngineConfiguration processEngineConfiguration) {
		return processEngineConfiguration.buildProcessEngine();
	}

	@Override
	public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		processEngineConfiguration.setJdbcUrl(dataSourceURL);
		processEngineConfiguration.setJdbcUsername(dataSourceUsername);
		processEngineConfiguration.setJdbcPassword(dataSourcePassword);
		processEngineConfiguration.setJdbcDriver(dataSourceDriver);
		processEngineConfiguration.setJdbcPingEnabled(false);
		processEngineConfiguration.setJdbcPingQuery("select 1;");
		processEngineConfiguration.setDatabaseSchemaUpdate(dataSourceSchemaUpdate);
		if (StringUtils.isNotEmpty(dataSourceSchema)) {
			processEngineConfiguration.setDatabaseSchema(dataSourceSchema);
		}
		if (StringUtils.isNotEmpty(historicLevel)) {
			processEngineConfiguration.setHistoryLevel(HistoryLevel.getHistoryLevelForKey(historicLevel));
		}
		populatePolyglot(processEngineConfiguration);
		processEngineConfiguration.setScriptingEngines(createScriptengines(processEngineConfiguration));
	}

	private ScriptingEngines createScriptengines(ProcessEngineConfigurationImpl processEngineConfiguration) {
		List<ResolverFactory> resolverFactories = new ArrayList<>();
		resolverFactories.add(new VariableScopeResolverFactory());
		resolverFactories.add(new BeansResolverFactory());
		return new ScriptingEngines(new NScriptBindingsFactory(processEngineConfiguration, resolverFactories));
	}

	/**
	 * 
	 * @param processEngineConfiguration
	 * @see com.oracle.truffle.js.scriptengine.GraalJSScriptEngine.MagicBindingsOptionSetter
	 */
	protected void populatePolyglot(ProcessEngineConfigurationImpl processEngineConfiguration) {
		if (processEngineConfiguration.getBeans() == null) {
			processEngineConfiguration.setBeans(new HashMap<>());
		}
		for (String polyglotOption : polyglotOptions) {
			processEngineConfiguration.getBeans().put(polyglotOption, true);
		}
	}

}
