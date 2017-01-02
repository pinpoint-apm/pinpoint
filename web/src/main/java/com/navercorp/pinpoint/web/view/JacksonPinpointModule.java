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
package com.navercorp.pinpoint.web.view;


import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Jongho Moon
 *
 */
public class JacksonPinpointModule extends Module {

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.Module#getModuleName()
     */
    @Override
    public String getModuleName() {
        return "pinpoint";
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.Module#version()
     */
    @SuppressWarnings("deprecation")
    @Override
    public Version version() {
        return new Version(1, 0, 4, null);
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.Module#setupModule(org.codehaus.jackson.map.Module.SetupContext)
     */
    @Override
    public void setupModule(SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(ServiceType.class, new ServiceTypeSerializer());
        
        context.addSerializers(serializers);
    }

}
