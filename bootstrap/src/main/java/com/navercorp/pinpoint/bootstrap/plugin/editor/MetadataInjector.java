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

package com.navercorp.pinpoint.bootstrap.plugin.editor;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataHolder;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataInitializationStrategy;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;
import com.navercorp.pinpoint.exception.PinpointException;

public class MetadataInjector implements ClassRecipe {
    
    private final MetadataHolder metadataHolder;
    private final MetadataInitializationStrategy strategy;
    
    public MetadataInjector(MetadataHolder metadataHolder) {
        this(metadataHolder, null);
    }
    
    public MetadataInjector(MetadataHolder metadataHolder, MetadataInitializationStrategy strategy) {
        this.metadataHolder = metadataHolder;
        this.strategy = strategy;
    }

    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        if (strategy == null) {
            target.addTraceValue(metadataHolder.getType());
        } else {
            if (strategy instanceof ByConstructor) {
                String javaExpression = "new " + ((ByConstructor)strategy).getClassName() + "();";
                target.addTraceValue(metadataHolder.getType(), javaExpression);
            } else {
                throw new PinpointException("Unsupported strategy: " + strategy);
            }
        }
    }
}
