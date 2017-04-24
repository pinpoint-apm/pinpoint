/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.instrument.classreading;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DefaultSimpleClassMetadata implements SimpleClassMetadata {

    private final int version;

    private final int accessFlag;

    private final String className;

    private final String superClassName;

    private final List<String> interfaceNames;

    private final byte[] classBinary;

    public DefaultSimpleClassMetadata(final int version, final int accessFlag, final String classInternalName, final String superClassInternalName, final List<String> interfaceInternalNames, final byte[] classBinary) {
        this.version = version;
        this.accessFlag = accessFlag;
        this.className = JavaAssistUtils.jvmNameToJavaName(classInternalName);

        if (superClassInternalName == null) {
            this.superClassName = null;
        } else {
            this.superClassName = JavaAssistUtils.jvmNameToJavaName(superClassInternalName);
        }

        if (interfaceInternalNames == null) {
            this.interfaceNames = Collections.EMPTY_LIST;
        } else {
            this.interfaceNames = Collections.unmodifiableList(JavaAssistUtils.jvmNameToJavaName(interfaceInternalNames));
        }

        this.classBinary = classBinary;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public int getAccessFlag() {
        return accessFlag;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuperClassName() {
        return superClassName;
    }

    @Override
    public List<String> getInterfaceNames() {
        return interfaceNames;
    }

    @Override
    public byte[] getClassBinary() {
        return classBinary;
    }
}
