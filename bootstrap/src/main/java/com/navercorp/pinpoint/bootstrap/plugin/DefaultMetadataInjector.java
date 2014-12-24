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

package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;
import com.navercorp.pinpoint.exception.PinpointException;

public class DefaultMetadataInjector implements MetadataInjector {
    
    private final String metadataAccessorTypeName;
    private final MetadataInitializationStrategy strategy;
    
    public DefaultMetadataInjector(String metadataAccessorTypeName, MetadataInitializationStrategy strategy) {
        this.metadataAccessorTypeName = metadataAccessorTypeName;
        this.strategy = strategy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        Class<?> type;
        try {
            type = classLoader.loadClass(metadataAccessorTypeName);
        } catch (ClassNotFoundException e) {
            throw new PinpointException("Fail to load metadata accessor: " + metadataAccessorTypeName, e);
        }
        
        if (!TraceValue.class.isAssignableFrom(type)) {
            throw new PinpointException("Given type " + metadataAccessorTypeName + " is not a subtype of TraceValue");
        }
        
        Class<? extends TraceValue> metadataAccessorType = (Class<? extends TraceValue>)type; 
        
        if (strategy == null) {
            target.addTraceValue(metadataAccessorType);
        } else {
            if (strategy instanceof ByConstructor) {
                String javaExpression = "new " + ((ByConstructor)strategy).getClassName() + "();";
                target.addTraceValue(metadataAccessorType, javaExpression);
            } else {
                throw new PinpointException("Unsupported strategy: " + strategy);
            }
        }
    }
}
