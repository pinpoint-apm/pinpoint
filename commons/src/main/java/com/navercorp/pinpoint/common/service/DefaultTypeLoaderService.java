/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.service;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.TypeProviderLoader;
import com.navercorp.pinpoint.common.plugin.Type;
import com.navercorp.pinpoint.common.util.ClassLoaderUtils;

import java.net.URL;
import java.util.List;

/**
 * @author emeroad
 */
public class DefaultTypeLoaderService implements TypeLoaderService {

    private final TypeProviderLoader loader;

    public DefaultTypeLoaderService() {
        this(ClassLoaderUtils.getDefaultClassLoader());
    }

    public DefaultTypeLoaderService(URL[] jarLists) {
        if (jarLists == null) {
            throw new NullPointerException("classLoader must not be null");
        }
        this.loader = new TypeProviderLoader(false);
        loader.load(jarLists);

        initAnnotationKey();
    }

    @Deprecated
    private void initAnnotationKey() {
        AnnotationKey.initialize(loader.getAnnotationKeys());
    }

    public DefaultTypeLoaderService(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }
        this.loader = new TypeProviderLoader(false);
        loader.load(classLoader);
    }

    @Override
    public List<Type> getTypes() {
        return loader.getTypes();
    }


}
