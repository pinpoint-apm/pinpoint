/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.shutdown;


import com.navercorp.pinpoint.profiler.ShutdownHookRegister;
import com.navercorp.pinpoint.profiler.instrument.classloading.JavaLangAccessHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;

/**
 * If  change the name of this class,
 * The changed class name must be applied in `com.navercorp.pinpoint.profile.context.provider.ShutdownHookRegisterProvider `
 *
 * @author Taejin Koo
 */
public class Java9ShutdownHookRegister implements ShutdownHookRegister {
    private static final Object OBJECT = JavaLangAccessHelper.getJavaLangAccessObject();
    private static final MethodHandle REGISTER_SHUTDOWN_HOOK_METHOD_HANDLE = JavaLangAccessHelper.getRegisterShutdownHookMethodHandle();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void register(Thread thread) {
        logger.info("register() started.");

        for (int i = 3; i < 10; i++) {
            try {
                REGISTER_SHUTDOWN_HOOK_METHOD_HANDLE.invoke(OBJECT, i, true, thread);
                logger.info("register() completed.");
                return;
            } catch (Throwable e) {
            }
        }

        Runtime.getRuntime().addShutdownHook(thread);
        logger.info("register() completed. (ShutdownHook registered in java.lang.Runtime.)");
    }

}
