/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.module;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.util.PropertyRollbackTemplate;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NettyPlatformDependent {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProfilerConfig profilerConfig;
    private final Properties properties;

    public NettyPlatformDependent(ProfilerConfig profilerConfig, Properties properties) {
        this.profilerConfig = profilerConfig;
        this.properties = properties;
    }

    public void setup() {

        final boolean tryReflectionSetAccessible = profilerConfig.readBoolean(GrpcTransportConfig.KEY_PROFILER_CONFIG_NETTY_TRY_REFLECTION_SET_ACCESSIBLE,
                GrpcTransportConfig.DEFAULT_NETTY_SYSTEM_PROPERTY_TRY_REFLECTIVE_SET_ACCESSIBLE);
        final PropertyRollbackTemplate template = new PropertyRollbackTemplate(properties);

        if (tryReflectionSetAccessible && JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9)) {
            // netty system property `io.netty.tryReflectionSetAccessible`
            final String keySystemProperty = GrpcTransportConfig.SYSTEM_PROPERTY_NETTY_TRY_REFLECTION_SET_ACCESSIBLE;
            final String tryReflectionSetAccessibleString = String.valueOf(tryReflectionSetAccessible);
            template.addKey(keySystemProperty, tryReflectionSetAccessibleString);
        }

        final boolean noPreferDirect = profilerConfig.readBoolean(GrpcTransportConfig.KEY_PROFILER_CONFIG_NETTY_NOPREFERDIRECT, false);
        if (noPreferDirect) {
            final String noPreferDirectKey = GrpcTransportConfig.SYSTEM_PROPERTY_NETTY_NOPREFERDIRECT;
            final String noPreferDirectValue = String.valueOf(noPreferDirect);
            template.addKey(noPreferDirectKey, noPreferDirectValue);
        }

        template.execute(new Runnable() {
            @Override
            public void run() {
                // for preload
                PlatformDependent.addressSize();
                logger.info("PlatformDependent.hasUnsafe:{}", PlatformDependent.hasUnsafe());
            }
        });

    }
}
