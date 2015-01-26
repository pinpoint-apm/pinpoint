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

/**
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class FieldSnooperInjector implements Injector {
    
    private final FieldSnooper snooper;
    private final String fieldName;
    
    public FieldSnooperInjector(FieldSnooper snooper, String fieldName) {
        this.snooper = snooper;
        this.fieldName = fieldName;
    }

    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        target.addGetter(snooper.getType(), fieldName);
    }
}
