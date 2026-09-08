/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.util.Objects;

/**
 * Evaluates the whole matcher condition of an index value and accumulates the time spent on it.
 */
class IndexValueMatcher {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TransformerMatcher transformerMatcher;

    IndexValueMatcher(final TransformerMatcher transformerMatcher) {
        this.transformerMatcher = Objects.requireNonNull(transformerMatcher, "transformerMatcher");
    }

    ClassFileTransformer match(final ClassLoader classLoader, final IndexValue indexValue, final ClassMetadataWrapper classMetadata) {
        final long startTime = System.currentTimeMillis();
        if (this.transformerMatcher.match(classLoader, indexValue.getOperand(), classMetadata.get())) {
            final long elapsedTime = indexValue.accumulatorTime(startTime);
            if (isDebug) {
                logger.debug("Matching time elapsed={}ms, accumulator={}ms, operand={}", elapsedTime, indexValue.getAccumulatorTimeMillis(), indexValue.getOperand());
            }
            return indexValue.getTransformer();
        }
        indexValue.accumulatorTime(startTime);
        return null;
    }
}
