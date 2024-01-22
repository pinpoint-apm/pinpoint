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
package com.navercorp.pinpoint.profiler.context.grpc.mapper;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.mapstruct.Condition;
import org.mapstruct.Named;
import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author intr3p1d
 */
public class MapperUtils {

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface IsNotEmptyString {
    }

    @Condition
    @IsNotEmptyString
    public static boolean isNotEmpty(String value) {
        return !StringUtils.isEmpty(value);
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface IsNotZeroShort {
    }


    @Condition
    @IsNotZeroShort
    public static boolean isNotZero(short value) {
        return value != 0;
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface IsNotZeroLong {
    }

    @Condition
    @IsNotZeroLong
    public static boolean isNotZero(long value) {
        return value != 0;
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface IsNotMinusOne {
    }

    @Condition
    @IsNotMinusOne
    public static boolean isNotMinusOne(int v) {
        return v != -1;
    }
}
