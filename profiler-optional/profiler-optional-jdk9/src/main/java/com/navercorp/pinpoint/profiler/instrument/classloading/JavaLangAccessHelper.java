/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jaehong.kim
 */
public final class JavaLangAccessHelper {
    private static final Logger logger = LogManager.getLogger(JavaLangAccessHelper.class);
    // Java 9 version over and after
    private static final String MISC_SHARED_SECRETS_CLASS_NAME = "jdk.internal.misc.SharedSecrets";
    private static final String MISC_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.misc.JavaLangAccess";
    // Java 12 version over and after
    private static final String ACCESS_SHARED_SECRETS_CLASS_NAME = "jdk.internal.access.SharedSecrets";
    private static final String ACCESS_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.access.JavaLangAccess";

    private static final JavaLangAccess JAVA_LANG_ACCESS = newJavaLangAccessor();

    private JavaLangAccessHelper() {
    }

    public static JavaLangAccess getJavaLangAccess() {
        return JAVA_LANG_ACCESS;
    }

    // for debugging
    private static void dumpJdkInfo() {
        logger.warn("Dump JDK info java.vm.name:{} java.version:{}", JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VM_NAME), JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VM_VERSION));
    }


    private static JavaLangAccess newJavaLangAccessor()  {
        try {
            Class.forName(MISC_JAVA_LANG_ACCESS_CLASS_NAME, false, JavaLangAccess.class.getClassLoader());
            return new JavaLangAccess9();
        } catch (ClassNotFoundException ignored) {
            // ignore
        }
        try {
            // https://github.com/naver/pinpoint/issues/6752
            // Oracle JDK11 : jdk.internal.access
            // openJDK11 =  jdk.internal.misc
            Class.forName(ACCESS_SHARED_SECRETS_CLASS_NAME, false, JavaLangAccess.class.getClassLoader());
            return new JavaLangAccess11();
        } catch (ClassNotFoundException ignored) {
            // ignore
        }

        dumpJdkInfo();
        throw new IllegalStateException("JavaLangAccess not found");
    }
}