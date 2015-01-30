/**
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
package com.navercorp.pinpoint.bootstrap.plugin.editor;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;

/**
 * @author Jongho Moon
 *
 */
public class CacheApiMethodRecipe implements MethodRecipe {
    private final ProfilerPluginContext context;

    
    public CacheApiMethodRecipe(ProfilerPluginContext context) {
        this.context = context;
    }
    
    @Override
    public void edit(ClassLoader targetClassLoader, InstrumentClass targetClass, MethodInfo targetMethod) throws InstrumentException {
        Integer id = context.cacheApi(targetMethod);
        
        if (id != null) {
            targetMethod.getDescriptor().setApiId(id);
        }
    }

}
