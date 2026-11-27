/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.java9.module;

import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ModuleSupportTest {

    @Test
    public void addPermissionToInjectedHelpers() throws IOException {
        URL[] urls = {CodeSourceUtils.getCodeLocation(LogManager.class)};
        Instrumentation instrumentation = mock(Instrumentation.class);
        ModuleSupport moduleSupport = new ModuleSupport(instrumentation, message -> { });

        for (int i = 0; i < 2; i++) {
            try (URLClassLoader classLoader = new URLClassLoader("agent.test" + i, urls, getClass().getClassLoader())) {
                Module agentModule = new ModuleBuilder(message -> { }).defineModule(classLoader.getName(), classLoader, urls);
                Module unnamedModule = classLoader.getUnnamedModule();
                Assertions.assertFalse(agentModule.getPackages().isEmpty());

                moduleSupport.addPermissionToInjectedHelpers(classLoader, new Java9Module(instrumentation, agentModule));

                verify(instrumentation).redefineModule(agentModule, Set.of(unnamedModule), Map.of(), Map.of(), Set.of(), Map.of());
                for (String packageName : agentModule.getPackages()) {
                    verify(instrumentation).redefineModule(agentModule, Set.of(), Map.of(packageName, Set.of(unnamedModule)), Map.of(), Set.of(), Map.of());
                }
            }
        }
        verifyNoMoreInteractions(instrumentation);
    }

    @Test
    public void defineAgentModuleGrantsHelperAccessBeforeServiceLoading() throws IOException {
        URL[] urls = {CodeSourceUtils.getCodeLocation(LogManager.class)};
        Instrumentation instrumentation = mock(Instrumentation.class);
        ModuleSupport moduleSupport = new ModuleSupport(instrumentation, message -> { });

        try (URLClassLoader classLoader = new URLClassLoader("agent.test", urls, getClass().getClassLoader())) {
            // The unit test has no agent distribution on the bootstrap class path.
            ModuleException exception = Assertions.assertThrows(ModuleException.class,
                    () -> moduleSupport.defineAgentModule(classLoader, urls));
            Assertions.assertInstanceOf(ClassNotFoundException.class, exception.getCause());
            Assertions.assertEquals("com.navercorp.pinpoint.common.trace.TraceMetadataProvider", exception.getCause().getMessage().replace('/', '.'));

            InOrder order = inOrder(instrumentation);
            ArgumentCaptor<Module> agentModule = ArgumentCaptor.forClass(Module.class);
            order.verify(instrumentation).redefineModule(agentModule.capture(), eq(Set.of(classLoader.getUnnamedModule())),
                    eq(Map.of()), eq(Map.of()), eq(Set.of()), eq(Map.of()));
            for (String packageName : agentModule.getValue().getPackages()) {
                order.verify(instrumentation).redefineModule(eq(agentModule.getValue()), eq(Set.of()),
                        eq(Map.of(packageName, Set.of(classLoader.getUnnamedModule()))), eq(Map.of()), eq(Set.of()), eq(Map.of()));
            }
            // prepareAgentModule starts by exporting the profiler package to the bootstrap module.
            order.verify(instrumentation).redefineModule(agentModule.getValue(), Set.of(),
                    Map.of("com.navercorp.pinpoint.profiler", Set.of(ModuleSupport.class.getModule())), Map.of(), Set.of(), Map.of());
        }
    }
}
