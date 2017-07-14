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
public class ClassInternalNameMatcherOperand extends AbstractMatcherOperand {
    private final String classInternalName;

    public ClassInternalNameMatcherOperand(final String className) {
        Assert.requireNonNull(className, "className must not be null");
        this.classInternalName = ClassUtils.toInternalName(className);
    }

    public String getClassInternalName() {
        return this.classInternalName;
    }

    public boolean match(final String classInternalName) {
        if (classInternalName != null) {
            return this.classInternalName.equals(classInternalName);
        }
        return false;
    }

    @Override
    public int getExecutionCost() {
        return 1;
    }

    @Override
    public boolean isIndex() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("classInternalName=").append(this.classInternalName);
        sb.append('}');
        return sb.toString();
    }
}