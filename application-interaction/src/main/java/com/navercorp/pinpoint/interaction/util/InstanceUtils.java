/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.interaction.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author yjqg6666
 */
public class InstanceUtils {

    private static final List<String> SERVLET_REQUEST_CLASSES = Arrays.asList("javax.servlet.ServletRequest", "javax.servlet.http.HttpServletRequest", "org.springframework.web.multipart.MultipartHttpServletRequest");

    private InstanceUtils() {
    }

    public static boolean isServletRequest(Object object) {
        if (object == null) {
            return false;
        }
        Class<?> objectClass = object.getClass();
        if (objectClass == null) {
            return false;
        }
        Class<?>[] interfaces = objectClass.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if (SERVLET_REQUEST_CLASSES.contains(anInterface.getName())) {
                return true;
            }
        }
        return false;
    }

}
