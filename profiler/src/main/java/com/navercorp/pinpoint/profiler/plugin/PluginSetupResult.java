/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginSetupResult {

    private final DefaultProfilerPluginSetupContext setupContext;
    private final ClassFileTransformerLoader transformerRegistry;

    public PluginSetupResult(DefaultProfilerPluginSetupContext setupContext, ClassFileTransformerLoader transformerRegistry) {
        this.setupContext = setupContext;
        this.transformerRegistry = transformerRegistry;
    }


    public List<ApplicationTypeDetector> getApplicationTypeDetectors() {
        return this.setupContext.getApplicationTypeDetectors();
    }

    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        return this.setupContext.getJdbcUrlParserList();
    }

    public List<ClassFileTransformer> getClassTransformerList() {
        return transformerRegistry.getClassTransformerList();
    }


}
