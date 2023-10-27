/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.collector.mapper;

import com.google.common.base.CharMatcher;
import org.mapstruct.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author intr3p1d
 */
@Component
public class ErrorMessageMapper {

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface ReplaceCharacters {
    }

    private static final CharMatcher SAFE_ASCII = CharMatcher.ascii()
            .or(CharMatcher.whitespace());
    private boolean replaceCharacters = false;

    public ErrorMessageMapper(@Value("${pinpoint.collector.exceptiontrace.replace.characters:true}") boolean replaceCharacters) {
        this.replaceCharacters = replaceCharacters;
    }

    @ReplaceCharacters
    public String replaceCharacters(String errorMessage) {
        if (replaceCharacters) {
            return SAFE_ASCII.retainFrom(errorMessage);
        }
        return errorMessage;
    }
}
