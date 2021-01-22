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

import com.navercorp.pinpoint.test.plugin.util.Assert;
import com.navercorp.pinpoint.test.plugin.util.StringUtils;
import com.navercorp.pinpoint.test.plugin.util.ThreadContextCallable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Taejin Koo
 */
public class ReflectionDependencyResolver {

    private final ClassLoader classLoader;
    private Object dependencyResolverObject;
    private Method resolveArtifactsAndDependenciesMethod;

    public ReflectionDependencyResolver(ClassLoader classLoader) {
        this.classLoader = Assert.requireNonNull(classLoader, "classLoader");
    }

    public List<File> lookup(final List<String> classpathList) throws Exception {
        String paths = StringUtils.join(classpathList, ArtifactIdUtils.ARTIFACT_SEPARATOR);
        return lookup(paths);
    }

    private List<File> lookup(final String classpath) throws Exception {
        synchronized (this) {
            if (dependencyResolverObject == null) {
                call(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        initialize();
                        return null;
                    }
                });
            }
        }

        try {
            return call(new Callable<List<File>>() {
                @Override
                public List<File> call() throws Exception {
                    return (List<File>) resolveArtifactsAndDependenciesMethod.invoke(dependencyResolverObject, classpath);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("[" + classpath + "] dependency resolve fail Caused by:" + e.getMessage(), e);
        }
    }

    private void initialize() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> factory = classLoader.loadClass("com.navercorp.pinpoint.test.plugin.DependencyResolverFactory");
        Constructor<?> factoryConstructor = factory.getConstructor(boolean.class);
        Object factoryObject = factoryConstructor.newInstance(false);
        Method resolverGet = factory.getMethod("get", String[].class);

        dependencyResolverObject = resolverGet.invoke(factoryObject, (Object) new String[]{});

        Class<?> dependencyResolverClazz = dependencyResolverObject.getClass();
        resolveArtifactsAndDependenciesMethod = dependencyResolverClazz.getMethod("resolveArtifactsAndDependencies", String.class);
    }

    private <T> T call(Callable<T> delegate) throws Exception {
        Callable<T> callable = new ThreadContextCallable<>(delegate, classLoader);
        return callable.call();
    }


}
