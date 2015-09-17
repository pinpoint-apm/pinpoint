/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import java.util.Formatter;

/**
 * @author emeroad
 */
public class CodeBuilder {

    private final StringBuilder codeBlock;
    private final Formatter formatter;

    public CodeBuilder() {
        this(1024);
    }

    public CodeBuilder(int bufferSize) {
        this.codeBlock = new StringBuilder(bufferSize);
        this.formatter = new Formatter(codeBlock);
    }

    public void begin() {
        codeBlock.append('{');
    }

    public void end() {
        codeBlock.append('}');
    }

    public void append(String code) {
        codeBlock.append(code);
    }

    public void format(String format, Object... args) {
        formatter.format(format, args);
    }

    public String toString() {
        return codeBlock.toString();
    }

}
