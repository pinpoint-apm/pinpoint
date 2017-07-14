/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.classloader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.ASMEngine;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.classloading.DebugTransformerClassInjector;
import com.navercorp.pinpoint.profiler.instrument.JavassistEngine;
import com.navercorp.pinpoint.profiler.plugin.ClassFileTransformerLoader;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformerGuardDelegate;
import com.navercorp.pinpoint.profiler.plugin.PluginInstrumentContext;
import com.navercorp.pinpoint.test.MockApplicationContext;
import javassist.ClassPool;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.common.util.Assert;


/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class TestClassLoader extends TransformClassLoader {

    private final Logger logger = Logger.getLogger(TestClassLoader.class.getName());

    private final MockApplicationContext applicationContext;
    private Translator instrumentTranslator;
    private final List<String> delegateClass;
    private final ClassFileTransformerLoader classFileTransformerLoader;
    private final InstrumentContext instrumentContext;

    public TestClassLoader(MockApplicationContext applicationContext) {
        Assert.requireNonNull(applicationContext, "applicationContext must not be null");

        this.applicationContext = applicationContext;

        this.classFileTransformerLoader = new ClassFileTransformerLoader(applicationContext.getProfilerConfig(), applicationContext.getDynamicTransformTrigger());

//        ClassInjector classInjector = new LegacyProfilerPluginClassInjector(getClass().getClassLoader());
        ClassInjector classInjector = new DebugTransformerClassInjector();
        this.instrumentContext = new PluginInstrumentContext(applicationContext.getProfilerConfig(), applicationContext.getInstrumentEngine(),
                applicationContext.getDynamicTransformTrigger(), classInjector, classFileTransformerLoader);

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
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("findClass className:{}" + name);
        }
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
        return applicationContext.getProfilerConfig();
    }

    public void addTransformer(final String targetClassName, final TransformCallback transformer) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addTransformer targetClassName:{}" + targetClassName + " callback:{}" + transformer);
        }
        final Matcher matcher = Matchers.newClassNameMatcher(targetClassName);
        final MatchableClassFileTransformerGuardDelegate guard = new MatchableClassFileTransformerGuardDelegate(applicationContext.getProfilerConfig(), instrumentContext, matcher, transformer);

        this.instrumentTranslator.addTransformer(guard);
    }

    private void addDefaultDelegateLoadingOf() {
        TestClassList testClassList = new TestClassList();
        for (String className : testClassList.getTestClassList()) {
            this.delegateLoadingOf(className);
        }
    }

    @Override
    protected Class<?> loadClassByDelegation(String name) throws ClassNotFoundException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("loadClassByDelegation className:{}" + name);
        }
        return super.loadClassByDelegation(name);
    }

    public void addTranslator() {
        final InstrumentEngine instrumentEngine = applicationContext.getInstrumentEngine();
        if (instrumentEngine instanceof JavassistEngine) {

            logger.info("JAVASSIST BCI engine");
            ClassPool classPool = ((JavassistEngine) instrumentEngine).getClassPool(this);
            this.instrumentTranslator = new JavassistTranslator(this, classPool, applicationContext.getClassFileTransformerDispatcher());
            this.addTranslator(instrumentTranslator);

        } else if (instrumentEngine instanceof ASMEngine) {

            logger.info("ASM BCI engine");
            this.instrumentTranslator = new DefaultTranslator(this, applicationContext.getClassFileTransformerDispatcher());
            this.addTranslator(instrumentTranslator);

        } else {

            logger.info("Unknown BCI engine");

            this.instrumentTranslator = new DefaultTranslator(this, applicationContext.getClassFileTransformerDispatcher());
            this.addTranslator(instrumentTranslator);
        }
    }
}
