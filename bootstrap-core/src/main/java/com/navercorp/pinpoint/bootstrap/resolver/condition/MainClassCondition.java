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

import java.io.IOException;
import java.util.jar.JarFile;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;

/**
 * @author HyunGil Jeong
 * 
 */
public class MainClassCondition implements Condition<String>, ConditionValue<String> {

    private static final String MANIFEST_MAIN_CLASS_KEY = "Main-Class";
    private static final String NOT_FOUND = null;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass().getName()); 

    private final String applicationMainClassName;

    public MainClassCondition() {
        this(SystemProperty.INSTANCE);
    }

    public MainClassCondition(SimpleProperty property) {
        if (property == null) {
            throw new IllegalArgumentException("properties should not be null");
        }
        this.applicationMainClassName = getMainClassName(property);
        if (this.applicationMainClassName == NOT_FOUND) {
            logger.info("Main class could not be deduced, please set 'profiler.applicationservertype' in pinpoint.config.");
            logger.info("If you're running on 1.6.0_24 or prior version of Java, consider upgrading to 1.6.0_25+.");
            logger.info("If you're running Tomcat or Tomcat on Spring Boot, please set 'profiler.tomcat.conditional.transform' to false");
            logger.info("If you're running Jboss, please set 'profiler.tomcat.conditional.transform' to false");
        }
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
        }
        if (this.applicationMainClassName.equals(condition)) {
            logger.debug("Main class match - [{}]", this.applicationMainClassName, condition);
            return true;
        } else {
            logger.debug("Main class does not match - found : [{}], expected : [{}]", this.applicationMainClassName, condition);
            return false;
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
            String jreVersion = property.getProperty(SystemPropertyKey.JAVA_RUNTIME_VERSION.getKey());
            logger.warn("Error retrieving main class using '{}', jre : {}", SystemPropertyKey.SUN_JAVA_COMMAND.getKey(), jreVersion);
            return NOT_FOUND;
        } else {
            JarFile executableArchive = null;
            try {
                executableArchive = new JarFile(javaCommand);
                return extractMainClassFromArchive(executableArchive);
            } catch (IOException e) {
                // If it's not a valid java archive, VM shouldn't start in the first place.
                // Thus this would simply be a main class
                return javaCommand;
            } catch (Exception e) {
                // fail-safe, application shouldn't not start because of this
                logger.warn("Error retrieving main class", e);
                return NOT_FOUND;
            } finally {
                if (executableArchive != null) {
                    try {
                        executableArchive.close();
                    } catch (IOException e) {
                        logger.warn("Error closing jarFile : [{}]", executableArchive.getName(), e);
                    }
                }
            }
        }
    }

    private String extractMainClassFromArchive(JarFile bootstrapJar) throws IOException {
        String mainClassFromManifest = bootstrapJar.getManifest().getMainAttributes().getValue(MANIFEST_MAIN_CLASS_KEY);
        if (mainClassFromManifest == null) {
            return NOT_FOUND;
        }
        return mainClassFromManifest;
    }

}
