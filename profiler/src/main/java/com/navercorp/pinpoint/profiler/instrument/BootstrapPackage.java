/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootstrapPackage {

    public BootstrapPackage() {
    }

    private static final String[] BOOTSTRAP_PACKAGE_LIST = {
            "com.navercorp.pinpoint.bootstrap",
            "com.navercorp.pinpoint.common",
            "com.navercorp.pinpoint.exception"
    };

    private static final String[] INTERNAL_BOOTSTRAP_PACKAGE_LIST = toInternalName(BOOTSTRAP_PACKAGE_LIST);

    private static String[] toInternalName(String[] bootstrapPackageList) {
        String[] internalPackageNames = new String[bootstrapPackageList.length];
        for (int i = 0; i < bootstrapPackageList.length; i++) {
            String packageName = bootstrapPackageList[i];
            internalPackageNames[i] = JavaAssistUtils.javaNameToJvmName(packageName);
        }
        return internalPackageNames;
    }


    public boolean isBootstrapPackage(String className) {
        if (className == null) {
            return false;
        }

        for (String bootstrapPackage : BOOTSTRAP_PACKAGE_LIST) {
            if (className.startsWith(bootstrapPackage)) {
                return true;
            }
        }
        return false;
    }


    public boolean isBootstrapPackageByInternalName(String internalClassName) {
        if (internalClassName == null) {
            return false;
        }

        for (String bootstrapPackage : INTERNAL_BOOTSTRAP_PACKAGE_LIST) {
            if (internalClassName.startsWith(bootstrapPackage)) {
                return true;
            }
        }
        return false;
    }


}
