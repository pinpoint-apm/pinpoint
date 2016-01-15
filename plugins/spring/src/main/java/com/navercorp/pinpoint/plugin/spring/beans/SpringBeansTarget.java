/**
 * Copyright 2016 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.spring.beans;

import java.util.regex.Pattern;

/**
 * @author jaehong.kim
 */
public class SpringBeansTarget {
    private Pattern namePattern;
    private Pattern classPattern;
    private String annotation;

    public boolean isValid() {
        if (namePattern != null || classPattern != null) {
            return true;
        }

        if (annotation != null && !annotation.isEmpty()) {
            return true;
        }

        return false;
    }

    public Pattern getNamePattern() {
        return namePattern;
    }

    public void setNamePattern(String namePatternRegex) {
        if (namePatternRegex != null) {
            this.namePattern = Pattern.compile(namePatternRegex.trim());
        } else {
            this.namePattern = null;
        }
    }

    public Pattern getClassPattern() {
        return classPattern;
    }

    public void setClassPattern(String classPatternRegex) {
        if (classPatternRegex != null) {
            this.classPattern = Pattern.compile(classPatternRegex.trim());
        } else {
            this.classPattern = null;
        }
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        if (annotation != null) {
            this.annotation = annotation.trim();
        } else {
            this.annotation = annotation;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpringBeansTarget{");
        sb.append("namePattern=").append(namePattern);
        sb.append(", classPattern=").append(classPattern);
        sb.append(", annotation='").append(annotation).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
