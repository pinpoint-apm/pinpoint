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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author HyunGil Jeong
 * 
 */
public class ClassResourceCondition implements Condition<String> {

    private static final String CLASS_EXTENSION = ".class";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass().getName()); 

    private String getClassNameAsResource(String className) {
        String classNameAsResource = className.replace('.', '/');
        return classNameAsResource.endsWith(CLASS_EXTENSION) ? classNameAsResource : classNameAsResource.concat(CLASS_EXTENSION);
    }
    
    /**
     * Checks if the specified class can be found in the current System ClassLoader's search path.
     * 
     * @param requiredClass the fully qualified class name of the class to check
     * @return <tt>true</tt> if the specified class can be found in the system class loader's search path, 
     *         <tt>false</tt> if otherwise
     */
    @Override
    public boolean check(String requiredClass) {
        if (StringUtils.isEmpty(requiredClass)) {
            return false;
        }
        String classNameAsResource = getClassNameAsResource(requiredClass);
        if (ClassLoader.getSystemResource(classNameAsResource) != null) {
            logger.debug("Resource found - [{}]", classNameAsResource);
            return true;
        } else {
            logger.debug("Resource not found - [{}]", classNameAsResource);
            return false;
        }
    }

}
