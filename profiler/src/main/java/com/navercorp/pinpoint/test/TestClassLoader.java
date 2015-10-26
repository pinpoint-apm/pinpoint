/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.test;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformerGuardDelegate;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.Loader;
import javassist.NotFoundException;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.common.util.Asserts;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.instrument.LegacyProfilerPluginClassInjector;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class TestClassLoader extends Loader {
    private final DefaultAgent agent;
    private final InstrumentTranslator instrumentTranslator;
    private final DefaultProfilerPluginContext context;
    private final List<String> delegateClass;

    public TestClassLoader(DefaultAgent agent) {
        Asserts.notNull(agent, "agent");
        
        this.agent = agent;
        this.context = new DefaultProfilerPluginContext(agent, new LegacyProfilerPluginClassInjector(getClass().getClassLoader()));
        this.instrumentTranslator = new InstrumentTranslator(this, agent.getClassFileTransformerDispatcher());
        this.delegateClass = new ArrayList<String>();
    }


    public void addDelegateClass(String className) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        this.delegateClass.add(className);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public void initialize() {
        addDefaultDelegateLoadingOf();
        addCustomDelegateLoadingOf();
        addTranslator();
    }

    private void addCustomDelegateLoadingOf() {
        for (String className : delegateClass) {
            this.delegateLoadingOf(className);
        }
    }

    public ProfilerConfig getProfilerConfig() {
        return agent.getProfilerConfig();
    }

    public void addTransformer(final String targetClassName, final TransformCallback transformer) {
        final Matcher matcher = Matchers.newClassNameMatcher(targetClassName);
        final MatchableClassFileTransformerGuardDelegate guard = new MatchableClassFileTransformerGuardDelegate(context, matcher, transformer);

        this.instrumentTranslator.addTransformer(guard);
    }

    private void addDefaultDelegateLoadingOf() {
        this.delegateLoadingOf("com.navercorp.pinpoint.bootstrap.");
        this.delegateLoadingOf("com.navercorp.pinpoint.common.");
        this.delegateLoadingOf("com.navercorp.pinpoint.thrift.");
        this.delegateLoadingOf("com.navercorp.pinpoint.profiler.context.");

        this.delegateLoadingOf("com.navercorp.pinpoint.test.MockAgent");
        this.delegateLoadingOf("com.navercorp.pinpoint.test.TBaseRecorder");
        this.delegateLoadingOf("com.navercorp.pinpoint.test.TBaseRecorderAdaptor");
        this.delegateLoadingOf("com.navercorp.pinpoint.test.ListenableDataSender");
        this.delegateLoadingOf("com.navercorp.pinpoint.test.ListenableDataSender$Listener");
        this.delegateLoadingOf("com.navercorp.pinpoint.test.ResettableServerMetaDataHolder");
        this.delegateLoadingOf("com.navercorp.pinpoint.test.junit4.TestContext");

        this.delegateLoadingOf("com.navercorp.pinpoint.test.junit4.IsRootSpan");
        this.delegateLoadingOf("org.apache.thrift.TBase");
        this.delegateLoadingOf("junit.");
        this.delegateLoadingOf("org.hamcrest.");
        this.delegateLoadingOf("org.junit.");
    }

    @Override
    protected Class<?> loadClassByDelegation(String name) throws ClassNotFoundException {
        return super.loadClassByDelegation(name);
    }

    private void addTranslator() {
        try {
            ClassPool classPool = agent.getClassPool().getClassPool(this);
            addTranslator(classPool, instrumentTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void runTest(String className, String methodName) throws Throwable {
        Class<?> c = loadClass(className);
        Object o = c.newInstance();
        try {
            c.getDeclaredMethod(methodName).invoke(o);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
