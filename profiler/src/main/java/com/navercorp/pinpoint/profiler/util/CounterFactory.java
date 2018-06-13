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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;

import java.lang.reflect.Constructor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CounterFactory {

    private static final PLogger logger = PLoggerFactory.getLogger(CounterFactory.class.getName());

    private static final ObjectFactory<Counter> counterFactory = buildFactory();

    private static ObjectFactory<Counter> buildFactory() {
        JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_8)) {
            String counterName = "com.navercorp.pinpoint.profiler.util.Java8CounterFactory";
            try {
                Class<ObjectFactory<Counter>> counterClazz = (Class<ObjectFactory<Counter>>) Class.forName(counterName, false, CounterFactory.class.getClassLoader());
                Constructor<ObjectFactory<Counter>> constructor = counterClazz.getDeclaredConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                logger.warn("{} not found", counterName , e);
            }
        }
        return new Java6CounterFactory();
    }

    public static Counter newCounter() {
        return counterFactory.newInstance();
    }

    interface ObjectFactory<T> {
        T newInstance();
    }

    private static class Java6CounterFactory implements ObjectFactory<Counter> {
        @Override
        public Counter newInstance() {
            return new Java6Counter();
        }
    };

}
