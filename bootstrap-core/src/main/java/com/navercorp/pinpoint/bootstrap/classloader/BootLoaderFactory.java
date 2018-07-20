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

package com.navercorp.pinpoint.bootstrap.classloader;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class BootLoaderFactory {

    private static final boolean isJ9Vm = isJ9VM();

    private BootLoaderFactory() {
    }


    public static BootLoader newBootLoader() {
        if (isJ9Vm) {
            try {
                return new J9BootLoader();
            } catch (Error linkageError) {
                // TODO log
                throw new IllegalStateException("J9BootLoader create fail Caused by:" + linkageError.getMessage(), linkageError);
            }
        }
        return new LauncherBootLoader();
    }

    private static boolean isJ9VM() {
        // ibm-j9 java.vm.name=IBM J9 VM;
        // openj9 java.vm.name=Eclipse OpenJ9 VM
//        if (System.getProperty("java.vm.name", "").contains("J9 VM")) {
//            return true;
//        }
        try {
            ClassLoader.class.getDeclaredField("bootstrapClassLoader");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

}
