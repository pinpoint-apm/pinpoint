/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Properties;

/**
 * @author HyunGil Jeong
 */
public class ContainerResolver {

    public static final String CONTAINER_PROPERTY_KEY = "pinpoint.container";

    private final BootLogger logger = BootLogger.getLogger(ContainerResolver.class.getName());

    private final Properties properties;

    public ContainerResolver() {
        this(System.getProperties());
    }

    public ContainerResolver(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }
        this.properties = properties;
    }

    /**
     * Returns <tt>true</tt> if {@value #CONTAINER_PROPERTY_KEY} property exists and has no value or is <tt>true</tt>.
     *
     * @return <tt>true</tt> if {@value #CONTAINER_PROPERTY_KEY} property exists and has no value or is <tt>true</tt>.
     */
    public boolean isContainer() {
        if (properties.containsKey(CONTAINER_PROPERTY_KEY)) {
            String value = properties.getProperty(CONTAINER_PROPERTY_KEY);
            if (StringUtils.isEmpty(value)) {
                logger.info("-D" + CONTAINER_PROPERTY_KEY + " found.");
                return true;
            }
            boolean isContainer = Boolean.parseBoolean(value);
            logger.info("-D" + CONTAINER_PROPERTY_KEY + " found : " + value + ", resolved to " + isContainer);
            return isContainer;
        }
        return false;
    }
}
