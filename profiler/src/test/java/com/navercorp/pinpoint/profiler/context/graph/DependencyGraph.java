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
import com.google.inject.Module;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.DefaultAgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.InterceptorRegistryModule;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.OverrideModuleFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.TestInterceptorRegistryBinder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DependencyGraph {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        ProfilerConfig profilerConfig = spy(new DefaultProfilerConfig());
        Mockito.when(profilerConfig.getStaticResourceCleanup()).thenReturn(true);

        Instrumentation instrumentation = mock(Instrumentation.class);
        AgentOption agentOption = new DefaultAgentOption(instrumentation,
                "mockAgent", "mockApplicationName", false, profilerConfig, Collections.<String>emptyList(),
                null);

        InterceptorRegistryBinder interceptorRegistryBinder = new TestInterceptorRegistryBinder();
        Module testInterceptorRegistryModule = InterceptorRegistryModule.wrap(interceptorRegistryBinder);
        ModuleFactory moduleFactory = new OverrideModuleFactory(testInterceptorRegistryModule);

        return new DefaultApplicationContext(agentOption, moduleFactory);
    }

    private String currentWorkingDir() {
        URL location = CodeSourceUtils.getCodeLocation(Logger.class);

        String dir = location.getPath();
        return dir;
    }

    public class Grapher {
        public void graph(String filename, Injector demoInjector) throws IOException {
            PrintWriter out = new PrintWriter(new File(filename), Charsets.UTF_8.name());

            Injector injector = Guice.createInjector(new GraphvizModule());
            GraphvizGrapher grapher = injector.getInstance(GraphvizGrapher.class);
            grapher.setOut(out);
            grapher.setRankdir("TB");
            grapher.graph(demoInjector);
        }
    }
}
