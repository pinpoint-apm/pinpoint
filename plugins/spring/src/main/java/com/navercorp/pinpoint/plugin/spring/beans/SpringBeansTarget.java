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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author jaehong.kim
 */
public class SpringBeansTarget {
    private List<Pattern> namePatterns;
    private List<Pattern> classPatterns;
    private List<String> annotations;

    public boolean isValid() {
        if (namePatterns != null && !namePatterns.isEmpty()) {
            return true;
        }

        if (classPatterns != null && !classPatterns.isEmpty()) {
            return true;
        }

        if (annotations != null && !annotations.isEmpty()) {
            return true;
        }

        return false;
    }

    public void setNamePatterns(String namePatternRegex) {
        this.namePatterns = compilePattern(split(namePatternRegex));
    }

    public List<Pattern> getNamePatterns() {
        return namePatterns;
    }

    public void setClassPatterns(String classPatternRegex) {
        this.classPatterns = compilePattern(split(classPatternRegex));
    }

    public List<Pattern> getClassPatterns() {
        return classPatterns;
    }

    public void setAnnotations(String annotations) {
        this.annotations = split(annotations);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    private List<String> split(String values) {
        if (values == null) {
            return Collections.emptyList();
        }

        String[] tokens = values.split(",");
        List<String> result = new ArrayList<String>(tokens.length);

        for (String token : tokens) {
            String trimmed = token.trim();

            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result;
    }

    private List<Pattern> compilePattern(List<String> patternStrings) {
        if (patternStrings == null || patternStrings.isEmpty()) {
            return null;
        }
        List<Pattern> beanNamePatterns = new ArrayList<Pattern>(patternStrings.size());
        for (String patternString : patternStrings) {
            Pattern pattern = Pattern.compile(patternString);
            beanNamePatterns.add(pattern);
        }
        return beanNamePatterns;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("namePatterns=").append(namePatterns);
        sb.append(", classPatterns=").append(classPatterns);
        sb.append(", annotations=").append(annotations);
        sb.append('}');
        return sb.toString();
    }
}