/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.web.problem;

import static java.util.ServiceLoader.load;
import static java.util.stream.StreamSupport.stream;

import java.util.Collection;
/**
 * <a href="https://github.com/zalando/problem/blob/main/problem/src/main/java/org/zalando/problem/spi/StackTraceProcessor.java">...</a>
 */
public interface StackTraceProcessor {

    StackTraceProcessor DEFAULT = elements -> elements;
    StackTraceProcessor COMPOUND = stream(load(StackTraceProcessor.class).spliterator(), false)
            .reduce(DEFAULT, (first, second) -> elements -> second.process(first.process(elements)));

    Collection<StackTraceElement> process(final Collection<StackTraceElement> elements);

}
