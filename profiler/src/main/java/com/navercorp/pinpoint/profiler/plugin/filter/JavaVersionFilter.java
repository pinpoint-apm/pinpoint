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

package com.navercorp.pinpoint.profiler.plugin.filter;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JavaVersionFilter implements PluginFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JvmVersion jvmVersion;

    public JavaVersionFilter() {
        this(JvmUtils.getVersion());
    }

    public JavaVersionFilter(JvmVersion jvmVersion) {
        this.jvmVersion = Assert.requireNonNull(jvmVersion, "jvmVersion");
    }

    @Override
    public boolean accept(PluginJar pluginJar) {
        String pluginId = pluginJar.getPluginId();
        if (pluginId == null) {
            logger.warn("Invalid plugin : {}, missing manifest entry : {}", pluginJar.getJarFile().getName(), PluginJar.PINPOINT_PLUGIN_ID);
            return REJECT;
        }
        String pluginCompilerVersion = pluginJar.getPluginCompilerVersion();
        if (pluginCompilerVersion == null) {
            logger.info("Skipping {} due to missing manifest entry : {}", pluginJar.getJarFile().getName(), PluginJar.PINPOINT_PLUGIN_COMPILER_VERSION);
            return REJECT;
        }
        JvmVersion pluginJvmVersion = JvmVersion.getFromVersion(pluginCompilerVersion);
        if (pluginJvmVersion == JvmVersion.UNSUPPORTED) {
            logger.info("Skipping {} due to unknown plugin compiler version : {}", pluginId, pluginCompilerVersion);
            return REJECT;
        }
        if (jvmVersion.onOrAfter(pluginJvmVersion)) {
            return ACCEPT;
        }
        logger.info("Skipping {} due to java version. Required : {}, found : {}", pluginId, pluginJvmVersion, jvmVersion);
        return REJECT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JavaVersionFilter{");
        sb.append("jvmVersion=").append(jvmVersion);
        sb.append('}');
        return sb.toString();
    }
}
