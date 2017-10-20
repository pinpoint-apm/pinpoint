/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.config;

/**
 * @author jaehong.kim
 */
public class InstrumentMatcherCacheConfig {

    private int interfaceCacheSize = 0;
    private int interfaceCacheEntrySize = 0;
    private int annotationCacheSize = 0;
    private int annotationCacheEntrySize = 0;
    private int superCacheSize = 0;
    private int superCacheEntrySize = 0;

    public int getInterfaceCacheSize() {
        return interfaceCacheSize;
    }

    public void setInterfaceCacheSize(int interfaceCacheSize) {
        this.interfaceCacheSize = interfaceCacheSize;
    }

    public int getInterfaceCacheEntrySize() {
        return interfaceCacheEntrySize;
    }

    public void setInterfaceCacheEntrySize(int interfaceCacheEntrySize) {
        this.interfaceCacheEntrySize = interfaceCacheEntrySize;
    }

    public int getAnnotationCacheSize() {
        return annotationCacheSize;
    }

    public void setAnnotationCacheSize(int annotationCacheSize) {
        this.annotationCacheSize = annotationCacheSize;
    }

    public int getAnnotationCacheEntrySize() {
        return annotationCacheEntrySize;
    }

    public void setAnnotationCacheEntrySize(int annotationCacheEntrySize) {
        this.annotationCacheEntrySize = annotationCacheEntrySize;
    }

    public int getSuperCacheSize() {
        return superCacheSize;
    }

    public void setSuperCacheSize(int superCacheSize) {
        this.superCacheSize = superCacheSize;
    }

    public int getSuperCacheEntrySize() {
        return superCacheEntrySize;
    }

    public void setSuperCacheEntrySize(int superCacheEntrySize) {
        this.superCacheEntrySize = superCacheEntrySize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("interfaceCacheSize=").append(interfaceCacheSize);
        sb.append(", interfaceCacheEntrySize=").append(interfaceCacheEntrySize);
        sb.append(", annotationCacheSize=").append(annotationCacheSize);
        sb.append(", annotationCacheEntrySize=").append(annotationCacheEntrySize);
        sb.append(", superCacheSize=").append(superCacheSize);
        sb.append(", superCacheEntrySize=").append(superCacheEntrySize);
        sb.append('}');
        return sb.toString();
    }
}