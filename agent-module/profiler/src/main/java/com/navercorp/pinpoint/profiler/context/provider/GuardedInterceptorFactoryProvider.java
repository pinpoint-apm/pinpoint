/*
 * Copyright 2026 NAVER Corp.
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
import com.navercorp.pinpoint.profiler.instrument.ASMGuardedInterceptorFactory;
import com.navercorp.pinpoint.profiler.instrument.DisableGuardedInterceptorFactory;
import com.navercorp.pinpoint.profiler.instrument.GuardedInterceptorFactory;
import com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore;

import java.util.Objects;

/**
 * Picks the guard wrapper strategy once from the config: the per-interceptor codegen, or the
 * disabled variant that leaves every interceptor on the shared wrapper. Bound as a singleton,
 * the codegen caches live on the returned instance.
 */
public class GuardedInterceptorFactoryProvider implements Provider<GuardedInterceptorFactory> {
    public static final String GUARD_CODEGEN_KEY = "profiler.interceptor.exception.guard.codegen";

    private final ProfilerConfig profilerConfig;
    private final BootstrapCore bootstrapCore;

    @Inject
    public GuardedInterceptorFactoryProvider(ProfilerConfig profilerConfig, BootstrapCore bootstrapCore) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.bootstrapCore = Objects.requireNonNull(bootstrapCore, "bootstrapCore");
    }

    @Override
    public GuardedInterceptorFactory get() {
        final boolean guardCodegen = profilerConfig.readBoolean(GUARD_CODEGEN_KEY, true);
        if (guardCodegen) {
            return new ASMGuardedInterceptorFactory(bootstrapCore);
        }
        return new DisableGuardedInterceptorFactory();
    }
}
