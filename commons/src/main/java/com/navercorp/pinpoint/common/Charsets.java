/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Woonduk Kang(emeroad)
 * @deprecated Since 2.3.0 Use {@link java.nio.charset.StandardCharsets}
 */
@Deprecated
public final class Charsets {
    private Charsets() {
    }

    public static final Charset US_ASCII = StandardCharsets.US_ASCII;
    public static final String US_ASCII_NAME = US_ASCII.name();

    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static final String UTF_8_NAME = UTF_8.name();


}
