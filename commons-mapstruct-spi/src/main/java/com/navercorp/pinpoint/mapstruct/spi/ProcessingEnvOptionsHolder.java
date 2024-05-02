/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.mapstruct.spi;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.google.common.collect.ImmutableMap;

/**
 * @author intr3p1d
 */
/**
 * This is not a true processor. It merely exists to pass the defined supported options to a global context that is accessible by the MapStruct classes which
 * would otherwise not have visibility to these.
 */
@SupportedAnnotationTypes({})
@SupportedOptions({ ProcessingEnvOptionsHolder.ENUM_POSTFIX_OVERRIDES })
public class ProcessingEnvOptionsHolder extends AbstractProcessor {

    static final String ENUM_POSTFIX_OVERRIDES = "mapstructSpi.enumPostfixOverrides";

    private Map<String, String> options;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        options = ImmutableMap.copyOf(processingEnv.getOptions());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public boolean containsKey(String key) {
        if (options == null) {
            throw new IllegalStateException("ProcessingEnvOptionsHolder not initialized yet.");
        }
        return options.containsKey(key);
    }

    public String getOption(String key) {
        if (options == null) {
            throw new IllegalStateException("ProcessingEnvOptionsHolder not initialized yet.");
        }
        return options.get(key);
    }
}