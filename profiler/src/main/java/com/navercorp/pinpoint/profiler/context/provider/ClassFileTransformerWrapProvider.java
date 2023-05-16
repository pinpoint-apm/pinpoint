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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import java.util.Objects;
import com.navercorp.pinpoint.profiler.transformer.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDumpService;
import com.navercorp.pinpoint.profiler.instrument.BytecodeDumpTransformer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.instrument.ClassFileTransformer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassFileTransformerWrapProvider implements Provider<ClassFileTransformer> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ProfilerConfig profilerConfig;
    private final Provider<ClassFileTransformerDispatcher> classFileTransformerDispatcherProvider;

    @Inject
    public ClassFileTransformerWrapProvider(ProfilerConfig profilerConfig, Provider<ClassFileTransformerDispatcher> classFileTransformerDispatcherProvider) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.classFileTransformerDispatcherProvider = Objects.requireNonNull(classFileTransformerDispatcherProvider, "classFileTransformerDispatcherProvider");
    }


    public ClassFileTransformer get() {

        ClassFileTransformerDispatcher classFileTransformerDispatcher = classFileTransformerDispatcherProvider.get();
        final boolean enableBytecodeDump = profilerConfig.readBoolean(ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP, ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP_DEFAULT_VALUE);
        if (enableBytecodeDump) {
            logger.info("wrapBytecodeDumpTransformer");
            return BytecodeDumpTransformer.wrap(classFileTransformerDispatcher, profilerConfig);
        }
        return classFileTransformerDispatcher;
    }
}
