/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ReflectionDependencyResolver {

    private static Object dependencyResolverObject;
    private static Method resolveArtifactsAndDependenciesMethod;

    static List<File> get(String classpath) throws Exception {
        if (dependencyResolverObject == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            Class<?> factory = classLoader.loadClass("com.navercorp.pinpoint.test.plugin.DependencyResolverFactory");
            Constructor<?> factoryConstructor = factory.getConstructor(boolean.class);
            Object factoryObject = factoryConstructor.newInstance(false);
            Method resolverGet = factory.getMethod("get", String[].class);

            dependencyResolverObject = resolverGet.invoke(factoryObject, (Object) new String[]{});

            Class<?> dependencyResolverClazz = dependencyResolverObject.getClass();
            resolveArtifactsAndDependenciesMethod = dependencyResolverClazz.getMethod("resolveArtifactsAndDependencies", String.class);
        }

        try {
            return (List<File>) resolveArtifactsAndDependenciesMethod.invoke(dependencyResolverObject, classpath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("dependency resolve fail Caused by:" + e.getMessage(), e);
        }
    }

}
