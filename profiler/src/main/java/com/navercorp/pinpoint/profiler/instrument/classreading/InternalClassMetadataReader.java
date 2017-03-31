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

import java.io.IOException;

/**
 * @author jaehong.kim
 */
public class InternalClassMetadataReader {
    private final InternalClassMetadata classMetadata;

    public static InternalClassMetadata readInternalClassMetadata(final byte[] classBinary) {
        final InternalClassMetadataReader internalClassMetadataReader = new InternalClassMetadataReader(new ClassReaderWrapper(classBinary, true));
        return internalClassMetadataReader.getInternalClassMetadata();
    }

    public static InternalClassMetadata readInternalClassMetadata(final ClassLoader classLoader, final String classInternalName) throws IOException {
        final InternalClassMetadataReader internalClassMetadataReader = new InternalClassMetadataReader(new ClassReaderWrapper(classLoader, classInternalName, true));
        return internalClassMetadataReader.getInternalClassMetadata();

    }

    InternalClassMetadataReader(final ClassReaderWrapper classReader) {
        this.classMetadata = new DefaultInternalClassMetadata(classReader.getClassInternalName(), classReader.getSuperClassInternalName(), classReader.getInterfaceInternalNames(), classReader.getAnnotationInternalNames(), classReader.isInterface(), classReader.isAnnotation(), classReader.isSynthetic(), classReader.isInnerClass());
    }

    public InternalClassMetadata getInternalClassMetadata() {
        return classMetadata;
    }
}