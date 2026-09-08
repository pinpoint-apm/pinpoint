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

import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadataReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads the class metadata lazily, at most once per class load.
 */
class ClassMetadataWrapper {
    private static final Logger logger = LogManager.getLogger(ClassMetadataWrapper.class);

    private final byte[] classFileBuffer;
    private InternalClassMetadata classMetadata;

    ClassMetadataWrapper(final byte[] classFileBuffer, final InternalClassMetadata classMetadata) {
        this.classFileBuffer = classFileBuffer;
        this.classMetadata = classMetadata;
    }

    public InternalClassMetadata get() {
        if (this.classMetadata == null) {
            try {
                this.classMetadata = InternalClassMetadataReader.readInternalClassMetadata(this.classFileBuffer);
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("Failed to read metadata of class bytes.", e);
                }
                return null;
            }
        }
        return this.classMetadata;
    }
}
