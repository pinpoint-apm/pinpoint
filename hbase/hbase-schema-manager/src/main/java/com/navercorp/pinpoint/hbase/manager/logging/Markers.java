/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.manager.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author HyunGil Jeong
 */
public class Markers {

    private static final String TERMINAL_MARKER = "TERMINAL";
    private static final String APP_LOG_MARKER = "APP_LOG";

    public static final Marker TERMINAL = MarkerFactory.getMarker(TERMINAL_MARKER);
    public static final Marker APP_LOG = MarkerFactory.getMarker(APP_LOG_MARKER);

}
