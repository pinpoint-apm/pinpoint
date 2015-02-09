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

package com.navercorp.pinpoint.profiler.plugin.editor;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;

public class ConditionalMethodEditor implements MethodEditor {
    private final ClassCondition condition;
    private final MethodEditor delegate;
    
    public ConditionalMethodEditor(ClassCondition condition, MethodEditor delegate) {
        this.condition = condition;
        this.delegate = delegate;
    }
    
    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        if (condition.check(classLoader, target)) {
            delegate.edit(classLoader, target);
        }
    }
}
