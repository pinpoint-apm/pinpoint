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

import com.navercorp.pinpoint.common.util.Asserts;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ClassReaderWrapper {
    // wrapped ASM ClassReader.
    private final ClassReader classReader;

    // bytecodes of the class.
    public ClassReaderWrapper(final byte[] classBinary) {
        Asserts.notNull(classBinary, "classBinary");
        this.classReader = new ClassReader(classBinary);
    }

    // classloader and class internal name.
    public ClassReaderWrapper(final ClassLoader classLoader, final String classInternalName) throws IOException {
        Asserts.notNull(classInternalName, "classInternalName");
        ClassLoader cl = classLoader;
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
            if (cl == null) {
                // system fail.
                throw new IOException("system classloader is null.");
            }
        }

        final InputStream in = cl.getResourceAsStream(classInternalName + ".class");
        if (in == null) {
            throw new IOException("not found class. classLoader=" + cl + ", classInternalName=" + classInternalName);
        }

        try {
            this.classReader = new ClassReader(in);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    // class's access flags.
    public int getAccess() {
        return this.classReader.getAccess();
    }

    // class version.
    public int getVersion() {
        return this.classReader.readShort(6);
    }

    public String getSuperClassInternalName() {
        return this.classReader.getSuperName();
    }

    public String getClassInternalName() {
        return this.classReader.getClassName();
    }

    public List<String> getInterfaceInternalNames() {
        return Arrays.asList(this.classReader.getInterfaces());
    }

    public byte[] getClassBinary() {
        return this.classReader.b;
    }

    public List<String> getAnnotationInternalNames() {
        // annotation visitor.
        final AnnotationMetadataVisitor annotationMetadataVisitor = new AnnotationMetadataVisitor();
        this.classReader.accept(annotationMetadataVisitor, 0);
        return annotationMetadataVisitor.getAnnotationInternalNames();
    }
}
