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

package com.navercorp.pinpoint.common.util;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class CodeSourceUtils {
    private CodeSourceUtils() {
    }

    public static URL getCodeLocation(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz must not be null");
        }
        final ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        return getCodeLocation(protectionDomain);
    }


    public static URL getCodeLocation(ProtectionDomain protectionDomain) {
        final CodeSource codeSource = protectionDomain == null ? null : protectionDomain.getCodeSource();
        return codeSource == null ? null : codeSource.getLocation();
    }
}
