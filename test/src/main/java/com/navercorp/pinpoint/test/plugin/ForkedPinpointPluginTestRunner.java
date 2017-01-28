/*
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
package com.navercorp.pinpoint.test.plugin;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;

/**
 * @author Jongho Moon
 *
 */
public class ForkedPinpointPluginTestRunner extends BlockJUnit4ClassRunner {
    private final String testId;
    private final boolean manageTraceObject;
    
    public ForkedPinpointPluginTestRunner(Class<?> testClass, String testId) throws InitializationError {
        super(testClass);
        
        this.testId = testId;
        this.manageTraceObject = !testClass.isAnnotationPresent(TraceObjectManagable.class);
    }

    @Override
    protected String getName() {
        return String.format("[%s]", testId);
    }

    @Override
    protected String testName(final FrameworkMethod method) {
        return String.format("%s[%s]", method.getName(), testId);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        final Statement fromSuper = super.methodBlock(method);
        final boolean manageTraceObject = this.manageTraceObject && (method.getAnnotation(TraceObjectManagable.class) == null);
        
        return new Statement() {
            
            @Override
            public void evaluate() throws Throwable {
                PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
                
                verifier.initialize(manageTraceObject);
                try {
                    fromSuper.evaluate();
                } finally {
                    verifier.cleanUp(manageTraceObject);
                }
            }
        };
    }
    
    
}
