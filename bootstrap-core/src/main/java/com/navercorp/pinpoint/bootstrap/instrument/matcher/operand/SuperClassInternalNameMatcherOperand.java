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
public class SuperClassInternalNameMatcherOperand extends AbstractMatcherOperand {
    private final String superClassInternalName;
    private final boolean considerHierarchy;
    private final boolean javaPackage;

    public SuperClassInternalNameMatcherOperand(final String superClassName, final boolean considerHierarchy) {
        Assert.requireNonNull(superClassName, "superClassName must not be null");
        this.superClassInternalName = ClassUtils.toInternalName(superClassName);
        this.considerHierarchy = considerHierarchy;
        this.javaPackage = this.superClassInternalName.startsWith("java/");
    }

    public String getSuperClassInternalNames() {
        return this.superClassInternalName;
    }

    public boolean isConsiderHierarchy() {
        return this.considerHierarchy;
    }

    public boolean match(final String superClassInternalName) {
        if (superClassInternalName != null) {
            return this.superClassInternalName.equals(superClassInternalName);
        }
        return false;
    }

    public boolean isJavaPackage() {
        return javaPackage;
    }

    @Override
    public int getExecutionCost() {
        return this.considerHierarchy ? 5 : 2;
    }

    @Override
    public boolean isIndex() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("superClassInternalName=").append(superClassInternalName);
        sb.append(", considerHierarchy=").append(considerHierarchy);
        sb.append('}');
        return sb.toString();
    }
}