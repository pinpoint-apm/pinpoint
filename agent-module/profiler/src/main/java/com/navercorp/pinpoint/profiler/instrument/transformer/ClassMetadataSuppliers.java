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

import java.util.function.Supplier;

/**
 * Creates the {@link Supplier} that hands the class metadata to the transformer indexes.
 */
final class ClassMetadataSuppliers {

    private ClassMetadataSuppliers() {
    }

    /**
     * @param classMetadata the metadata already parsed by the caller, or {@code null} to parse it lazily from the class bytes
     */
    static Supplier<InternalClassMetadata> of(final byte[] classFileBuffer, final InternalClassMetadata classMetadata) {
        if (classMetadata != null) {
            return () -> classMetadata;
        }
        return new LazyClassMetadataSupplier(classFileBuffer);
    }

    /**
     * Parses the class metadata from the class bytes on the first {@link #get()} and keeps it for the rest of the class load.
     * Returns {@code null} if the class bytes cannot be parsed.
     */
    private static class LazyClassMetadataSupplier implements Supplier<InternalClassMetadata> {
        private static final Logger logger = LogManager.getLogger(LazyClassMetadataSupplier.class);

        private final byte[] classFileBuffer;
        // parsed once, null until then
        private InternalClassMetadata classMetadata;

        LazyClassMetadataSupplier(final byte[] classFileBuffer) {
            this.classFileBuffer = classFileBuffer;
        }

        @Override
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
}
