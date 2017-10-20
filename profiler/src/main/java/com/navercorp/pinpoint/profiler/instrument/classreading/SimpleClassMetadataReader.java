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

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleClassMetadataReader {

    private final SimpleClassMetadata simpleClassMetadata;

    public static SimpleClassMetadata readSimpleClassMetadata(final byte[] classBinary) {
        final SimpleClassMetadataReader simpleClassMetadataReader = new SimpleClassMetadataReader(new ClassReaderWrapper(classBinary));
        return simpleClassMetadataReader.getSimpleClassMetadata();
    }

    SimpleClassMetadataReader(final ClassReaderWrapper classReader) {
        this.simpleClassMetadata = new DefaultSimpleClassMetadata(classReader.getVersion(), classReader.getAccess(), classReader.getClassInternalName(), classReader.getSuperClassInternalName(), classReader.getInterfaceInternalNames(), classReader.getClassBinary());
    }

    public SimpleClassMetadata getSimpleClassMetadata() {
        return simpleClassMetadata;
    }
}