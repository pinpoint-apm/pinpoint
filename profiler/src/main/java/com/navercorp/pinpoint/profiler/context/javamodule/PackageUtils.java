/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.javamodule;

import com.navercorp.pinpoint.common.util.ClassUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
final class PackageUtils {

    static String getPackageNameFromInternalName(String className) {
        if (className == null) {
            throw new NullPointerException("className");
        }
        final String jvmPackageName = ClassUtils.getPackageName(className, '/', null);
        if (jvmPackageName == null) {
            return null;
        }

        return JavaAssistUtils.jvmNameToJavaName(jvmPackageName);
    }
}
