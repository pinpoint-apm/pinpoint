/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultLambdaClassFileResolver implements LambdaClassFileResolver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String resolve(ClassLoader classLoader, String classInternalName, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (classInternalName != null) {
            return classInternalName;
        }

        // proxy-like class specific for lambda expressions.
        // e.g. Example$$Lambda$1/1072591677
        try {
            final InternalClassMetadata classMetadata = InternalClassMetadataReader.readInternalClassMetadata(classFileBuffer);
            return classMetadata.getClassInternalName();
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to read metadata of lambda expressions. classLoader={}", classLoader, e);
            }
            return null;
        }

    }
}
