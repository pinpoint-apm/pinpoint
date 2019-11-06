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
 */
package com.navercorp.pinpoint.profiler.instrument.mock;

import java.io.IOException;

/**
 * @author jaehong.kim
 */
public class ExceptionClass {

    public void throwable() throws Throwable {
        throw new Throwable("throwable");
    }

    public void exception() throws Exception {
        throw new Exception("exception");
    }

    public void runtime() {
        throw new RuntimeException("runtime");
    }

    public void io() throws IOException {
        throw new IOException("io");
    }

    public void io2() throws IOException {
    }

    public void condition() {
        if (true) {
            runtime();
        }
    }
}
