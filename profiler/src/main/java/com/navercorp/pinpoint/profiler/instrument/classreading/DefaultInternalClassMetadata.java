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
package com.navercorp.pinpoint.profiler.instrument.classreading;

import java.util.Collections;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class DefaultInternalClassMetadata implements InternalClassMetadata {
    private final String classInternalName;
    private final String superClassInternalName;
    private final List<String> interfaceInternalNames;
    private final List<String> annotationInternalNames;
    private final boolean isInterface;
    private final boolean isAnnotation;
    private final boolean isSynthetic;
    private final boolean isInnerClass;

    public DefaultInternalClassMetadata(final String classInternalName, final String superClassInternalName, final List<String> interfaceInternalNames, final List<String> annotationInternalNames, final boolean isInterface, final boolean isAnnotation, final boolean isSynthetic, final boolean isInnerClass) {
        this.classInternalName = classInternalName;
        this.superClassInternalName = superClassInternalName;

        if (interfaceInternalNames == null) {
            this.interfaceInternalNames = Collections.EMPTY_LIST;
        } else {
            this.interfaceInternalNames = Collections.unmodifiableList(interfaceInternalNames);
        }

        if (annotationInternalNames == null) {
            this.annotationInternalNames = Collections.EMPTY_LIST;
        } else {
            this.annotationInternalNames = Collections.unmodifiableList(annotationInternalNames);
        }

        this.isInterface = isInterface;
        this.isAnnotation = isAnnotation;
        this.isSynthetic = isSynthetic;
        this.isInnerClass = isInnerClass;
    }

    @Override
    public String getClassInternalName() {
        return this.classInternalName;
    }

    @Override
    public String getSuperClassInternalName() {
        return this.superClassInternalName;
    }

    @Override
    public List<String> getInterfaceInternalNames() {
        return this.interfaceInternalNames;
    }

    @Override
    public List<String> getAnnotationInternalNames() {
        return this.annotationInternalNames;
    }

    @Override
    public boolean isInterface() {
        return this.isInterface;
    }

    @Override
    public boolean isAnnotation() {
        return this.isAnnotation;
    }

    public boolean isSynthetic() {
        return isSynthetic;
    }

    public boolean isInnerClass() {
        return isInnerClass;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("classInternalName='").append(classInternalName).append('\'');
        sb.append(", superClassInternalName='").append(superClassInternalName).append('\'');
        sb.append(", interfaceInternalNames=").append(interfaceInternalNames);
        sb.append(", annotationInternalNames=").append(annotationInternalNames);
        sb.append(", isInterface=").append(isInterface);
        sb.append(", isAnnotation=").append(isAnnotation);
        sb.append(", isSynthetic=").append(isSynthetic);
        sb.append(", isInnerClass=").append(isInnerClass);
        sb.append('}');
        return sb.toString();
    }
}