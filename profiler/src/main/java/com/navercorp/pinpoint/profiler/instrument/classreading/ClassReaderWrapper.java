/**
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.navercorp.pinpoint.profiler.instrument.classreading;

import com.navercorp.pinpoint.common.util.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ClassReaderWrapper {
    // wrapped ASM ClassReader.
    private final ClassReader classReader;

    // need readAttributes.
    private final List<String> annotationInternalNames = new ArrayList<String>();
    private boolean innerClass = false;

    // bytecodes of the class.
    public ClassReaderWrapper(final byte[] classBinary) {
        this(classBinary, false);
    }

    public ClassReaderWrapper(final byte[] classBinary, final boolean readAttributes) {
        Assert.requireNonNull(classBinary, "classBinary must not be null");
        this.classReader = new ClassReader(classBinary);
        if (readAttributes) {
            readAttributes();
        }
    }

    public ClassReaderWrapper(final ClassLoader classLoader, final String classInternalName) throws IOException {
        this(classLoader, classInternalName, false);
    }

    // classloader and class internal name.
    public ClassReaderWrapper(final ClassLoader classLoader, final String classInternalName, final boolean readAttributes) throws IOException {
        Assert.requireNonNull(classInternalName, "classInternalName must not be null");
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
            if (readAttributes) {
                readAttributes();
            }
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
        return this.annotationInternalNames;
    }

    public boolean isInnerClass() {
        return this.innerClass;
    }

    public boolean isInterface() {
        return (this.classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
    }

    public boolean isAnnotation() {
        return (this.classReader.getAccess() & Opcodes.ACC_ANNOTATION) != 0;
    }

    public boolean isSynthetic() {
        return (this.classReader.getAccess() & Opcodes.ACC_SYNTHETIC) != 0;
    }

    private void readAttributes() {
        final char[] c = new char[this.classReader.getMaxStringLength()]; // buffer used to read strings
        int u = getAttributes();
        int anns = 0;
        int ianns = 0;

        for (int i = this.classReader.readUnsignedShort(u); i > 0; --i) {
            final String attrName = this.classReader.readUTF8(u + 2, c);
            if ("EnclosingMethod".equals(attrName)) {
                // is inner class.
                if (this.classReader.readClass(u + 8, c) != null) {
                    this.innerClass = true;
                }
            } else if ("RuntimeVisibleAnnotations".equals(attrName)) {
                // annotation.
                anns = u + 8;
            } else if ("RuntimeInvisibleAnnotations".equals(attrName)) {
                // annotation.
                ianns = u + 8;
            }
            u += 6 + this.classReader.readInt(u + 4);
        }

        if (anns != 0) {
            readAnnotationInternalName(anns, c);
        }
        if (ianns != 0) {
            readAnnotationInternalName(ianns, c);
        }
    }

    /**
     * Returns the start index of the attribute_info structure of this class.
     *
     * @return the start index of the attribute_info structure of this class.
     */
    private int getAttributes() {
        // skips the header
        int header = this.classReader.header;
        int u = header + 8 + this.classReader.readUnsignedShort(header + 6) * 2;
        // skips fields and methods
        for (int i = this.classReader.readUnsignedShort(u); i > 0; --i) {
            for (int j = this.classReader.readUnsignedShort(u + 8); j > 0; --j) {
                u += 6 + this.classReader.readInt(u + 12);
            }
            u += 8;
        }
        u += 2;
        for (int i = this.classReader.readUnsignedShort(u); i > 0; --i) {
            for (int j = this.classReader.readUnsignedShort(u + 8); j > 0; --j) {
                u += 6 + this.classReader.readInt(u + 12);
            }
            u += 8;
        }
        // the attribute_info structure starts just after the methods
        return u + 2;
    }

    private void readAnnotationInternalName(int annotationIndex, final char[] buf) {
        for (int i = this.classReader.readUnsignedShort(annotationIndex), v = annotationIndex + 2; i > 0; --i) {
            final String annotationDesc = this.classReader.readUTF8(v, buf);
            final Type type = Type.getType(annotationDesc);
            if (type.getSort() == Type.OBJECT) {
                final String internalName = type.getInternalName();
                if (internalName != null) {
                    this.annotationInternalNames.add(internalName);
                }
            }
            v = readAnnotationValues(v + 2, buf, true);
        }
    }

    private int readAnnotationValues(int v, final char[] buf, final boolean named) {
        int i = this.classReader.readUnsignedShort(v);
        v += 2;
        if (named) {
            for (; i > 0; --i) {
                v = readAnnotationValue(v + 2, buf);
            }
        } else {
            for (; i > 0; --i) {
                v = readAnnotationValue(v, buf);
            }
        }
        return v;
    }

    private int readAnnotationValue(int v, final char[] buf) {
        switch (this.classReader.b[v] & 0xFF) {
            case 'e': // enum_const_value
                return v + 5;
            case '@': // annotation_value
                return readAnnotationValues(v + 3, buf, true);
            case '[': // array_value
                return readAnnotationValues(v + 1, buf, false);
            default:
                return v + 3;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder().append("{");
        sb.append("access=").append(classReader.getAccess()).append(", ");
        sb.append("name=").append(classReader.getClassName()).append(", ");
        sb.append("interfaces=").append(Arrays.asList(classReader.getInterfaces())).append(", ");
        sb.append("super=").append(classReader.getSuperName());
        sb.append("}");
        return sb.toString();
    }
}