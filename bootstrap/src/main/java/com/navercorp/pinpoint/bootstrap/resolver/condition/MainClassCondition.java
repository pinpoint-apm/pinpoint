/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.resolver.condition;

import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;

/**
 * Checks if the application's main class is in one of those specified in {@link MainClassCondition}.
 * <p>
 * The application's main class is extracted by reading the <code>sun.java.command</code> system property, and in cases
 * of executable jars, the <code>Main-Class</code> attribute inside the manifest file.
 * 
 * @author HyunGil Jeong
 */
public class MainClassCondition implements Condition<String>, ConditionValue<String> {

    private final Logger logger = Logger.getLogger(MainClassCondition.class.getName()); 

    private static final String MANIFEST_MAIN_CLASS_KEY = "Main-Class";
    private static final String NOT_FOUND = null;

    private final String applicationMainClassName;

    public MainClassCondition() {
        this(SystemProperty.INSTANCE);
    }

    MainClassCondition(SimpleProperty property) {
        if (property == null) {
            throw new IllegalArgumentException("properties should not be null");
        }
        this.applicationMainClassName = getMainClassName(property);
    }

    /**
     * Checks if the specified value matches the fully qualified class name of the application's main class.
     * If the main class cannot be resolved, the method return <tt>false</tt>.
     * 
     * @param condition the value to check against the application's main class name
     * @return <tt>true</tt> if the specified value matches the name of the main class; 
     *         <tt>false</tt> if otherwise, or if the main class cannot be resolved
     */
    @Override
    public boolean check(String condition) {
        if (this.applicationMainClassName == NOT_FOUND) {
            return false;
        } else {
            return this.applicationMainClassName.equals(condition);
        }
    }
    
    /**
     * Returns the fully qualified class name of the application's main class.
     * 
     * @return the fully qualified class name of the main class, or an empty string if the main class cannot be resolved
     */
    @Override
    public String getValue() {
        if (this.applicationMainClassName == NOT_FOUND) {
            return "";
        }
        return this.applicationMainClassName;
    }

    private String getMainClassName(SimpleProperty property) {
        String javaCommand = property.getProperty(SystemPropertyKey.SUN_JAVA_COMMAND.getKey(), "").split(" ")[0];
        if (javaCommand.isEmpty()) {
            logger.log(Level.WARNING, "Error retrieving main class from " + property.getClass().getName());
            return NOT_FOUND;
        } else if (javaCommand.endsWith(".jar")) {
            return extractMainClassFromJar(javaCommand);
        } else {
            return javaCommand;
        }
    }

    private String extractMainClassFromJar(String jarName) {
        try {
            JarFile bootstrapJar = new JarFile(jarName);
            return bootstrapJar.getManifest().getMainAttributes().getValue(MANIFEST_MAIN_CLASS_KEY);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Error retrieving main class from jar file : " + jarName, t);
            return NOT_FOUND;
        }
    }

}
