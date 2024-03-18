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

package com.navercorp.pinpoint.profiler.logging.jul;

import org.apache.logging.log4j.Level;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JulLogLevel {
    private JulLogLevel() {
    }

    public static final int JUL_ALL     = Integer.MIN_VALUE;
    public static final int JUL_FINEST  = 300;
    public static final int JUL_FINER   = 400;
    public static final int JUL_FINE    = 500;
    public static final int JUL_CONFIG  = 700;
    public static final int JUL_INFO    = 800;
    public static final int JUL_WARNING = 900;
    public static final int JUL_SEVERE  = 1000;
    public static final int JUL_OFF     = Integer.MAX_VALUE;

    public static Level toLevel(int julLevel) {
        switch (julLevel) {
            case JUL_ALL:
                return Level.ALL;
            case JUL_FINEST:
            case JUL_FINER:
                return Level.TRACE;
            case JUL_FINE:
            case JUL_CONFIG:
                return Level.DEBUG;
            case JUL_INFO:
                return Level.INFO;
            case JUL_WARNING:
                return Level.WARN;
            case JUL_SEVERE:
                return Level.ERROR;
            case JUL_OFF:
                return Level.OFF;
            default:
                return Level.OFF;
        }
    }
}
