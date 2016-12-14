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

    private final List<String> interfaceNameList;

    private final byte[] classBinary;

    private Class<?> definedClass;

    public DefaultSimpleClassMetadata(int version, int accessFlag, String className, String superClassName, String[] interfaceNameList, byte[] classBinary) {
        this.version = version;
        this.accessFlag = accessFlag;
        this.className = JavaAssistUtils.jvmNameToJavaName(className);
        this.superClassName = JavaAssistUtils.jvmNameToJavaName(superClassName);
        this.interfaceNameList = JavaAssistUtils.jvmNameToJavaName(interfaceNameList);
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
        return interfaceNameList;
    }

    @Override
    public byte[] getClassBinary() {
        return classBinary;
    }

    public void setDefinedClass(final Class<?> definedClass) {
        this.definedClass = definedClass;
    }

    @Override
    public Class<?> getDefinedClass() {
        return this.definedClass;
    }
}
