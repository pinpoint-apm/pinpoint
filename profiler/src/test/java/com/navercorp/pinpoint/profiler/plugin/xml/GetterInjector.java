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

package com.navercorp.pinpoint.profiler.plugin.xml;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.ClassRecipe;

/**
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class GetterInjector implements ClassRecipe {
    private final String getterTypeName;
    private final String fieldName;
    
    public GetterInjector(String getterTypeName, String fieldName) {
        this.getterTypeName = getterTypeName;
        this.fieldName = fieldName;
    }

    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        target.addGetter(getterTypeName, fieldName);
    }

    @Override
    public String toString() {
        return "GetterInjector [getterTypeName=" + getterTypeName + ", fieldName=" + fieldName + "]";
    }
}
