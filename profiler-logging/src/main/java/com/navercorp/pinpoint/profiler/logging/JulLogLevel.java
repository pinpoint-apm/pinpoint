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

package com.navercorp.pinpoint.profiler.logging;

import org.apache.logging.log4j.Level;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JulLogLevel {
    private JulLogLevel() {
    }

    public static Level toLevel(int julLevel) {
        switch (julLevel) {
            case Integer.MIN_VALUE:
                return Level.ALL;
            case 300:
            case 400:
                return Level.TRACE;
            case 500:
            case 700:
                return Level.DEBUG;
            case 800:
                return Level.INFO;
            case 900:
                return Level.WARN;
            case 1000:
                return Level.ERROR;
            case Integer.MAX_VALUE:
                return Level.OFF;
            default:
                return Level.OFF;
        }
    }
}
