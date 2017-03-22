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
 */
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jaehong.kim
 */
public final class ASMClassWriter extends ClassWriter {
    private static final String OBJECT_CLASS_INTERNAL_NAME = "java/lang/Object";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstrumentContext pluginContext;
    private ClassLoader classLoader;
    private String classInternalName;
    private String superClassInternalName;

    public ASMClassWriter(final InstrumentContext pluginContext, final String classInternalName, final String superClassInternalName, final int flags, final ClassLoader classLoader) {
        super(flags);
        this.pluginContext = pluginContext;
        this.classInternalName = classInternalName;
        this.superClassInternalName = superClassInternalName;
        this.classLoader = classLoader;
    }

    @Override
    protected String getCommonSuperClass(String type1ClassInternalName, String type2ClassInternalName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting common super class. type1ClassInternalName={}, type2ClassInternalName={}", type1ClassInternalName, type2ClassInternalName);
        }

        final String classInternalName = get(type1ClassInternalName, type2ClassInternalName);

        if (logger.isDebugEnabled()) {
            logger.debug("Common super class is '{}'. type1ClassInternalName={}, type2ClassInternalName={}", classInternalName, type1ClassInternalName, type2ClassInternalName);
        }

        return classInternalName;
    }

    private String get(final String type1ClassInternalName, final String type2ClassInternalName) {
        if (type1ClassInternalName == null || type1ClassInternalName.equals(OBJECT_CLASS_INTERNAL_NAME) || type2ClassInternalName == null || type2ClassInternalName.equals(OBJECT_CLASS_INTERNAL_NAME)) {
            // object is the root of the class hierarchy.
            return OBJECT_CLASS_INTERNAL_NAME;
        }

        if (type1ClassInternalName.equals(type2ClassInternalName)) {
            // two equal.
            return type1ClassInternalName;
        }

        // current class.
        if (type1ClassInternalName.equals(classInternalName)) {
            return getCommonSuperClass(superClassInternalName, type2ClassInternalName);
        } else if (type2ClassInternalName.equals(classInternalName)) {
            return getCommonSuperClass(type1ClassInternalName, superClassInternalName);
        }

        ClassReader type1ClassReader = getClassReader(type1ClassInternalName);
        ClassReader type2ClassReader = getClassReader(type2ClassInternalName);
        if (type1ClassReader == null || type2ClassReader == null) {
            logger.warn("Skip get common super class. not found class {type1ClassInternalName={}, reader={}}, {type2ClassInternalName={}, reader={}}", type1ClassInternalName, type1ClassReader, type2ClassInternalName, type2ClassReader);
            return OBJECT_CLASS_INTERNAL_NAME;
        }

        // interface.
        if (isInterface(type1ClassReader)) {
            String interfaceInternalName = type1ClassInternalName;
            if (isImplements(interfaceInternalName, type2ClassReader)) {
                return interfaceInternalName;
            }
            if (isInterface(type2ClassReader)) {
                interfaceInternalName = type2ClassInternalName;
                if (isImplements(interfaceInternalName, type1ClassReader)) {
                    return interfaceInternalName;
                }
            }
            return OBJECT_CLASS_INTERNAL_NAME;
        }

        // interface.
        if (isInterface(type2ClassReader)) {
            String interfaceName = type2ClassInternalName;
            if (isImplements(interfaceName, type1ClassReader)) {
                return interfaceName;
            }
            return OBJECT_CLASS_INTERNAL_NAME;
        }

        // class.
        final Set<String> superClassNames = new HashSet<String>();
        superClassNames.add(type1ClassInternalName);
        superClassNames.add(type2ClassInternalName);

        String type1SuperClassName = type1ClassReader.getSuperName();
        if (!superClassNames.add(type1SuperClassName)) {
            // find common superClass.
            return type1SuperClassName;
        }

        String type2SuperClassName = type2ClassReader.getSuperName();
        if (!superClassNames.add(type2SuperClassName)) {
            // find common superClass.
            return type2SuperClassName;
        }

        while (type1SuperClassName != null || type2SuperClassName != null) {
            if (type1SuperClassName != null) {
                type1SuperClassName = getSuperClassInternalName(type1SuperClassName);
                if (type1SuperClassName != null) {
                    if (!superClassNames.add(type1SuperClassName)) {
                        return type1SuperClassName;
                    }
                }
            }

            if (type2SuperClassName != null) {
                type2SuperClassName = getSuperClassInternalName(type2SuperClassName);
                if (type2SuperClassName != null) {
                    if (!superClassNames.add(type2SuperClassName)) {
                        return type2SuperClassName;
                    }
                }
            }
        }

        return OBJECT_CLASS_INTERNAL_NAME;
    }


    private boolean isInterface(final ClassReader classReader) {
        return (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
    }

    private boolean isImplements(final String interfaceInternalName, final ClassReader classReader) {
        ClassReader classInfo = classReader;

        while (classInfo != null) {
            final String[] interfaceInternalNames = classInfo.getInterfaces();
            for (String name : interfaceInternalNames) {
                if (name != null && name.equals(interfaceInternalName)) {
                    return true;
                }
            }

            for (String name : interfaceInternalNames) {
                if(name != null) {
                    final ClassReader interfaceInfo = getClassReader(name);
                    if (interfaceInfo != null) {
                        if (isImplements(interfaceInternalName, interfaceInfo)) {
                            return true;
                        }
                    }
                }
            }

            final String superClassInternalName = classInfo.getSuperName();
            if (superClassInternalName == null || superClassInternalName.equals(OBJECT_CLASS_INTERNAL_NAME)) {
                break;
            }
            classInfo = getClassReader(superClassInternalName);
        }

        return false;
    }


    private String getSuperClassInternalName(final String classInternalName) {
        final ClassReader classReader = getClassReader(classInternalName);
        if (classReader == null) {
            return null;
        }

        return classReader.getSuperName();
    }

    private ClassReader getClassReader(final String classInternalName) {
        InputStream in = null;
        try {
            in = pluginContext.getResourceAsStream(this.classLoader, classInternalName + ".class");
            if (in != null) {
                return new ClassReader(in);
            }
        } catch (IOException ignored) {
            // not found class.
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        return null;
    }
}