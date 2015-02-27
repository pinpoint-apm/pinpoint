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

/**
 * Checks whether the specified classes are currently accessible by the System ClassLoader. 
 * 
 * @author HyunGil Jeong
 */
public class LibraryClassCondition implements Condition<String[]> {

    private static final String CLASS_EXTENSION = ".class";

    private String getClassNameAsResource(String className) {
        String classNameAsResource = className.replace('.', '/');
        return classNameAsResource.endsWith(CLASS_EXTENSION) ? classNameAsResource : classNameAsResource.concat(CLASS_EXTENSION);
    }
    
    /**
     * Checks if the specified classes can be found in the current System ClassLoader's search path.
     * 
     * @param requiredClasses the fully qualified class names of the classes to check
     * @return <tt>true</tt> if all of the specified classes can be found in the system class loader's search path, 
     *         <tt>false</tt> if otherwise
     */
    @Override
    public boolean check(String ... requiredClasses) {
        if (requiredClasses == null) {
            return false;
        }
        for (String condition : requiredClasses) {
            String classNameAsResource = getClassNameAsResource(condition);
            if (ClassLoader.getSystemResource(classNameAsResource) == null) {
                return false;
            }
        }
        return true;
    }

}
