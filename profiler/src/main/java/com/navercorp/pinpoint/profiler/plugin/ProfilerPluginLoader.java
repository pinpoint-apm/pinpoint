/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.plugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.GuardInstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.JarProfilerPluginClassInjector;

/**
 * @author Jongho Moon
 *
 */
public class ProfilerPluginLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DefaultAgent agent;

    public ProfilerPluginLoader(DefaultAgent agent) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        this.agent = agent;
    }
    
    public List<DefaultProfilerPluginContext> load(URL[] pluginJars) {
        List<DefaultProfilerPluginContext> pluginContexts = new ArrayList<DefaultProfilerPluginContext>(pluginJars.length);
        List<String> disabled = agent.getProfilerConfig().getDisabledPlugins();
        
        for (URL jar : pluginJars) {
            List<ProfilerPlugin> plugins = PluginLoader.load(ProfilerPlugin.class, new URL[] { jar });
            
            for (ProfilerPlugin plugin : plugins) {
                if (disabled.contains(plugin.getClass().getName())) {
                    logger.info("Skip disabled plugin: {}", plugin.getClass().getName());
                    continue;
                }
                
                logger.info("Loading plugin: {}", plugin.getClass().getName());

                PluginConfig pluginConfig = new PluginConfig(jar, plugin, agent.getInstrumentation(), agent.getClassPool(), agent.getBootstrapCoreJar());
                final DefaultProfilerPluginContext context = setupPlugin(pluginConfig);
                pluginContexts.add(context);
            }
        }
        
        
        return pluginContexts;
    }

    private GuardInstrumentContext preparePlugin(ProfilerPlugin plugin, InstrumentContext context) {
        final GuardInstrumentContext guardInstrumentContext = new GuardInstrumentContext(context);
        if (plugin instanceof TransformTemplateAware) {
            if (logger.isDebugEnabled()) {
                logger.debug("setTransformTemplate {}", plugin.getClass().getName());
            }
            final TransformTemplate transformTemplate = new TransformTemplate(guardInstrumentContext);
            ((TransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        }
        return guardInstrumentContext;
    }

    private DefaultProfilerPluginContext setupPlugin(PluginConfig pluginConfig) {
        final ClassInjector classInjector = new JarProfilerPluginClassInjector(pluginConfig);
        final DefaultProfilerPluginContext context = new DefaultProfilerPluginContext(agent, classInjector);

        final GuardProfilerPluginContext guardPluginContext = new GuardProfilerPluginContext(context);
        final GuardInstrumentContext guardInstrumentContext = preparePlugin(pluginConfig.getPlugin(), context);
        try {
            // WARN external plugin api
            final ProfilerPlugin plugin = pluginConfig.getPlugin();
            plugin.setup(guardPluginContext);
        } finally {
            guardPluginContext.close();
            guardInstrumentContext.close();
        }
        return context;
    }

}
