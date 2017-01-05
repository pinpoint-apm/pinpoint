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
    private static final String OBJECT = "java/lang/Object";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstrumentContext pluginContext;
    private ClassLoader classLoader;
    private String className;
    private String superClassName;

    public ASMClassWriter(final InstrumentContext pluginContext, final String className, final String superClassName, final int flags, final ClassLoader classLoader) {
        super(flags);
        this.pluginContext = pluginContext;
        this.className = className;
        this.superClassName = superClassName;
        this.classLoader = classLoader;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting common super class. type1={}, type2={}", type1, type2);
        }

        final String className = get(type1, type2);

        if (logger.isDebugEnabled()) {
            logger.debug("Common super class is '{}'. type1={}, type2={}", className, type1, type2);
        }

        return className;
    }

    private String get(final String type1, final String type2) {
        if (type1 == null || type1.equals(OBJECT) || type2 == null || type2.equals(OBJECT)) {
            // object is the root of the class hierarchy.
            return OBJECT;
        }

        if (type1.equals(type2)) {
            // two equal.
            return type1;
        }

        // current class.
        if (type1.equals(className)) {
            return getCommonSuperClass(superClassName, type2);
        } else if (type2.equals(className)) {
            return getCommonSuperClass(type1, superClassName);
        }

        ClassReader type1ClassReader = getClassReader(type1);
        ClassReader type2ClassReader = getClassReader(type2);
        if (type1ClassReader == null || type2ClassReader == null) {
            logger.warn("Skip get common super class. not found class {type1={}, reader={}}, {type2={}, reader={}}", type1, type1ClassReader, type2, type2ClassReader);
            return OBJECT;
        }

        // interface.
        if (isInterface(type1ClassReader)) {
            String interfaceName = type1;
            if (isImplements(interfaceName, type2ClassReader)) {
                return interfaceName;
            }
            if (isInterface(type2ClassReader)) {
                interfaceName = type2;
                if (isImplements(interfaceName, type1ClassReader)) {
                    return interfaceName;
                }
            }
            return OBJECT;
        }

        // interface.
        if (isInterface(type2ClassReader)) {
            String interfaceName = type2;
            if (isImplements(interfaceName, type1ClassReader)) {
                return interfaceName;
            }
            return OBJECT;
        }

        // class.
        final Set<String> superClassNames = new HashSet<String>();
        superClassNames.add(type1);
        superClassNames.add(type2);

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
                type1SuperClassName = getSuperClassName(type1SuperClassName);
                if (type1SuperClassName != null) {
                    if (!superClassNames.add(type1SuperClassName)) {
                        return type1SuperClassName;
                    }
                }
            }

            if (type2SuperClassName != null) {
                type2SuperClassName = getSuperClassName(type2SuperClassName);
                if (type2SuperClassName != null) {
                    if (!superClassNames.add(type2SuperClassName)) {
                        return type2SuperClassName;
                    }
                }
            }
        }

        return OBJECT;
    }


    private boolean isInterface(final ClassReader classReader) {
        return (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
    }

    private boolean isImplements(final String interfaceName, final ClassReader classReader) {
        ClassReader classInfo = classReader;

        while (classInfo != null) {
            final String[] interfaceNames = classInfo.getInterfaces();
            for (String name : interfaceNames) {
                if (name != null && name.equals(interfaceName)) {
                    return true;
                }
            }

            for (String name : interfaceNames) {
                if(name != null) {
                    final ClassReader interfaceInfo = getClassReader(name);
                    if (interfaceInfo != null) {
                        if (isImplements(interfaceName, interfaceInfo)) {
                            return true;
                        }
                    }
                }
            }

            final String superClassName = classInfo.getSuperName();
            if (superClassName == null || superClassName.equals(OBJECT)) {
                break;
            }
            classInfo = getClassReader(superClassName);
        }

        return false;
    }


    private String getSuperClassName(final String className) {
        final ClassReader classReader = getClassReader(className);
        if (classReader == null) {
            return null;
        }

        return classReader.getSuperName();
    }

    private ClassReader getClassReader(final String className) {
        InputStream in = null;
        try {
            in = pluginContext.getResourceAsStream(this.classLoader, className + ".class");
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