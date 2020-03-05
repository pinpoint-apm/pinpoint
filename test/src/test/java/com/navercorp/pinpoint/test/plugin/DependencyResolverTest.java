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
package com.navercorp.pinpoint.test.plugin;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jongho Moon
 *
 */
public class DependencyResolverTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() {
        DependencyResolverFactory factory = new DependencyResolverFactory();
        DependencyResolver resolver = factory.get();
        resolver.resolveDependencySets("junit:junit:[4.12,4.13)");
    }
    
    @Test
    public void testClassifier() {
        DependencyResolverFactory factory = new DependencyResolverFactory();
        DependencyResolver resolver = factory.get();
        Map<String, List<Artifact>> sets = resolver.resolveDependencySets("net.sf.json-lib:json-lib:jar:jdk15:2.4");
        assertFalse(sets.isEmpty());
    }

    @Test
    public void resolveArtifactsAndDependencies() throws DependencyResolutionException, ArtifactResolutionException {
        DependencyResolverFactory factory = new DependencyResolverFactory();
        DependencyResolver resolver = factory.get();

        Map<String, List<Artifact>> sets = resolver.resolveDependencySets("org.eclipse.aether:aether-util:[0,)", "org.eclipse.aether:aether-spi");

        int i = 0;
        for (Map.Entry<String, List<Artifact>> set : sets.entrySet()) {
            logger.debug("{}", i++);
            List<File> results = resolver.resolveArtifactsAndDependencies(set.getValue());

            logger.debug(set.getKey());

            for (File result : results) {
                logger.debug("{}", result);
            }
        }
    }

}
