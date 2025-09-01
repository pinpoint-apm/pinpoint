/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.graph;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import com.navercorp.pinpoint.profiler.AgentContextOption;
import com.navercorp.pinpoint.profiler.AgentContextOptionBuilder;
import com.navercorp.pinpoint.profiler.AgentOption;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.v1.ObjectNameV1;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DependencyGraph {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public static void main(String[] args) throws IOException {
        DependencyGraph graph = new DependencyGraph();
        graph.dumpDependencyGraph();
    }

    public void dumpDependencyGraph() throws IOException {

        DefaultApplicationContext applicationContext = newApplicationContext();
        try {

            Injector injector = applicationContext.getInjector();

            String path = currentWorkingDir();
            String fileName = path + "../DependencyGraph.dot";
            logger.debug("filename:{}", fileName);

            Grapher grapher = new Grapher();
            grapher.graph(fileName, injector);

        } finally {
            applicationContext.close();
        }
    }

    private DefaultApplicationContext newApplicationContext() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();

        Instrumentation instrumentation = mock(Instrumentation.class);

        AgentOption agentOption = new AgentOption(instrumentation,
                profilerConfig.getProperties(), Collections.emptyMap(), null,
                Collections.emptyList(), Collections.emptyList(), false);

        ModuleFactoryResolver moduleFactoryResolver = new DefaultModuleFactoryResolver();
        ModuleFactory moduleFactory = moduleFactoryResolver.resolve();
        ObjectName objectName = new ObjectNameV1("mockAgentId", "mockAgentName", "mockApplicationName");

        AgentContextOption agentContextOption = AgentContextOptionBuilder.build(agentOption,
                objectName, profilerConfig);
        return new DefaultApplicationContext(agentContextOption, moduleFactory);
    }

    private String currentWorkingDir() {
        URL location = CodeSourceUtils.getCodeLocation(Logger.class);

        return location.getPath();
    }

    public static class Grapher {
        public void graph(String filename, Injector demoInjector) throws IOException {
            PrintWriter out = new PrintWriter(filename, StandardCharsets.UTF_8.name());

            Injector injector = Guice.createInjector(new GraphvizModule());
            GraphvizGrapher grapher = injector.getInstance(GraphvizGrapher.class);
            grapher.setOut(out);
            grapher.setRankdir("TB");
            grapher.graph(demoInjector);
        }
    }
}
