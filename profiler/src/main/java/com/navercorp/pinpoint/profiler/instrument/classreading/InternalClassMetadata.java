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

import java.util.List;

/**
 * @author jaehong.kim
 */
public interface InternalClassMetadata {

    // internal name of the class.
    String getClassInternalName();

    // internal name of the super class.
    String getSuperClassInternalName();

    // internal names of the class's interfaces.
    List<String> getInterfaceInternalNames();

    // internal names of the class's annotations.
    List<String> getAnnotationInternalNames();

    boolean isInterface();

    boolean isAnnotation();

    boolean isSynthetic();

    boolean isInnerClass();
}