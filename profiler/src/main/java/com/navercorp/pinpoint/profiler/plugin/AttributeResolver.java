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
package com.navercorp.pinpoint.profiler.plugin;

import java.lang.annotation.Annotation;

import com.navercorp.pinpoint.bootstrap.plugin.Attribute;
import com.navercorp.pinpoint.bootstrap.plugin.PluginContext;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class AttributeResolver implements ParameterResolver {
    private final PluginContext context;

    public AttributeResolver(PluginContext context) {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.plugin.ParameterResolver#resolve(int, java.lang.Class, java.lang.annotation.Annotation[])
     */
    @Override
    public Option<Object> resolve(int index, Class<?> type, Annotation[] annotations) {
        Attribute attribute = TypeUtils.findAnnotation(annotations, Attribute.class);
        
        if (attribute == null) {
            return Option.<Object>empty();
        }
        
        Object value = context.getAttribute(attribute.value());
        
        if (!type.isInstance(value)) {
            throw new PinpointException("attribute " + attribute.value() + "(" + value.getClass().getName() + ") is not assignable to " + type.getName());
        }
        
        return Option.withValue(value);
    }
}
