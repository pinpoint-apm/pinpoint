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
package com.navercorp.pinpoint.bootstrap.instrument.matcher.operand;

import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ClassUtils;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class AnnotationInternalNameMatcherOperand extends AbstractMatcherOperand {
    private final String annotationInternalName;
    private final boolean considerMetaAnnotation;

    public AnnotationInternalNameMatcherOperand(final String annotationName, final boolean considerMetaAnnotation) {
        Assert.requireNonNull(annotationName, "annotationName must not be null");
        this.annotationInternalName = ClassUtils.toInternalName(annotationName);
        this.considerMetaAnnotation = considerMetaAnnotation;
    }

    public String getAnnotationInternalName() {
        return annotationInternalName;
    }

    public boolean isConsiderMetaAnnotation() {
        return considerMetaAnnotation;
    }

    public boolean match(final String annotationInternalName) {
        if (annotationInternalName != null) {
            return this.annotationInternalName.equals(annotationInternalName);
        }
        return false;
    }

    @Override
    public int getExecutionCost() {
        return this.considerMetaAnnotation ? 5 : 2;
    }

    @Override
    public boolean isIndex() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("annotationInternalName=").append(annotationInternalName);
        sb.append(", considerMetaAnnotation=").append(considerMetaAnnotation);
        sb.append('}');
        return sb.toString();
    }
}