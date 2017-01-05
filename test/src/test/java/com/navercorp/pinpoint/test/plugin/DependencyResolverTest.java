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

import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.junit.Test;

/**
 * @author Jongho Moon
 *
 */
public class DependencyResolverTest {

    @Test
    public void test() {
        DependencyResolver resolver = DependencyResolver.get();
        resolver.resolveDependencySets("junit:junit:[4.12,4.13)");
    }
    
    @Test
    public void testClassifier() {
        DependencyResolver resolver = DependencyResolver.get();
        Map<String, List<Artifact>> sets = resolver.resolveDependencySets("net.sf.json-lib:json-lib:jar:jdk15:2.4");
        assertFalse(sets.isEmpty());
    }

}
