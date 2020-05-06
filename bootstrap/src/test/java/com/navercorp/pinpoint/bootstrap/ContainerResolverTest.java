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

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author HyunGil Jeong
 */
public class ContainerResolverTest {

    @Test
    public void noKeyShouldReturnFalse() {
        Properties properties = new Properties();
        ContainerResolver containerResolver = new ContainerResolver(properties);
        Assert.assertFalse(containerResolver.isContainer());
    }

    @Test
    public void noKeyAndDockerEnvShouldReturnFalse() {
        Properties properties = new Properties();
        ContainerResolver containerResolver = new ContainerResolver(properties);
        Assert.assertFalse(containerResolver.isContainer());
    }

    @Test
    public void emptyValueShouldReturnTrue() {
        Properties properties = new Properties();
        properties.put(ContainerResolver.CONTAINER_PROPERTY_KEY, "");
        ContainerResolver containerResolver = new ContainerResolver(properties);
        Assert.assertTrue(containerResolver.isContainer());
    }

    @Test
    public void trueValueShouldReturnTrue() {
        Properties properties = new Properties();
        properties.put(ContainerResolver.CONTAINER_PROPERTY_KEY, "tRue");
        ContainerResolver containerResolver = new ContainerResolver(properties);
        Assert.assertTrue(containerResolver.isContainer());
    }

    @Test
    public void falseValueShouldReturnFalse() {
        Properties properties = new Properties();
        properties.put(ContainerResolver.CONTAINER_PROPERTY_KEY, "FALSE");
        ContainerResolver containerResolver = new ContainerResolver(properties);
        Assert.assertFalse(containerResolver.isContainer());
    }
}
