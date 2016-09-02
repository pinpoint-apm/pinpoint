/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.google.common.logging;

/**
 * compiler trick class
 * @author Woonduk Kang(emeroad)
 */
public enum Level {

    OFF(Integer.MAX_VALUE),
    SEVERE(1000),
    WARNING(900),
    CONFIG(700),
    INFO(800),
    FINE(500),
    FINER(400),
    FINEST(300),
    ALL(Integer.MIN_VALUE);



    private final int level;

    Level(int level) {
        this.level = level;
    }

    public int intValue() {
        return level;
    }

    public String getName() {
        return name();
    }
}
