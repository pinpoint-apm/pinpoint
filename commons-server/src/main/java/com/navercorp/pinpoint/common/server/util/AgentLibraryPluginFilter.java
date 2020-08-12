/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.util.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Avoid agent library conflict
 * @author Woonduk Kang(emeroad)
 */
public class AgentLibraryPluginFilter implements Filter<URL> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String BOOTSTRAP = "com/navercorp/pinpoint/bootstrap/PinpointBootStrap.class";

    private final String agentDirectory;

    public AgentLibraryPluginFilter() {
        this(BOOTSTRAP);
    }

    public AgentLibraryPluginFilter(String jvmClassName) {
        final URL resource = this.getClass().getClassLoader().getResource(jvmClassName);
        if (resource == null) {
            throw new IllegalArgumentException("ClassName not found " + jvmClassName);
        }
        this.agentDirectory = resolveJarDirectory(resource);
        logger.info("AgentDirectory: {}", agentDirectory);
    }

    static String resolveJarDirectory(URL resource) {
        if (!resource.getProtocol().equalsIgnoreCase("jar")) {
            throw new IllegalArgumentException("Unexpected jar resource " + resource);
        }
        final String classPath = resource.getPath();
        final int jarIndex = classPath.lastIndexOf("!/");
        if (jarIndex == -1) {
            throw new IllegalArgumentException("jarIndex(!/) not found " + classPath);
        }
        final String jarPath = classPath.substring(0, jarIndex);
        final int dirIndex = jarPath.lastIndexOf('/');
        if (dirIndex == -1) {
            throw new IllegalArgumentException("AgentPath('/') not found " + jarPath);
        }
        String externalForm = jarPath.substring(0, dirIndex);
        final int protocolIndex = externalForm.indexOf('/');
        if (protocolIndex == -1) {
            throw new IllegalArgumentException("protocolIndex index not found " + externalForm);
        }
        return externalForm.substring(protocolIndex);
    }

    @Override
    public boolean filter(URL url) {
        if (url == null || url.getPath() == null) {
            return FILTERED;
        }
        // url.getPath().startWith(agentDirectory)
        if (url.getPath().contains(agentDirectory)) {
            logger.info("FILTERED AgentLibrary {}", url);
            return FILTERED;
        }
        logger.info("ACCEPT {}", url);
        return NOT_FILTERED;
    }
}
