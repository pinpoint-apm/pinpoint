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

import org.objectweb.asm.ClassReader;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleClassMetadataReader {

    private final SimpleClassMetadata simpleClassMetadata;

    public static SimpleClassMetadata readSimpleClassMetadata(byte[] classBinary) {
        SimpleClassMetadataReader simpleClassMetadataReader = new SimpleClassMetadataReader(classBinary);
        return simpleClassMetadataReader.getSimpleClassMetadata();
    }

    SimpleClassMetadataReader(byte[] classBinary) {
        if (classBinary == null) {
            throw new NullPointerException("classBinary must not be null");
        }

        final ClassReader classReader = new ClassReader(classBinary);

        int accessFlag = classReader.getAccess();
        String className = classReader.getClassName();
        String superClassName = classReader.getSuperName();
        String[] interfaceNameList = classReader.getInterfaces();

//        int offset = 0;
//        int version = classReader.readShort(offset + 6);
//         offset is zero
        int version = classReader.readShort(6);

        this.simpleClassMetadata = new DefaultSimpleClassMetadata(version, accessFlag, className, superClassName, interfaceNameList, classBinary);
    }

    public SimpleClassMetadata getSimpleClassMetadata() {
        return simpleClassMetadata;
    }


}
