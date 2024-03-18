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

package com.navercorp.pinpoint.profiler.instrument.lambda.mock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefineAnonymousClassDelegator {

    private static final Logger logger = LogManager.getLogger(DefineAnonymousClassDelegator.class);
    public static int count;

    public static Class<?> delegate(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        logger.debug("invoke delegate");
        count++;
        return null;
    }
}
