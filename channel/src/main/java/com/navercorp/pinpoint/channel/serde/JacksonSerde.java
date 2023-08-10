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
package com.navercorp.pinpoint.channel.serde;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class JacksonSerde<T> implements Serde<T> {

    private final ObjectMapper objectMapper;
    private final JavaType javaType;

    public JacksonSerde(ObjectMapper objectMapper, JavaType javaType) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.javaType = Objects.requireNonNull(javaType, "javaType");
    }

    public static <T> Serde<T> byClass(ObjectMapper objectMapper, Class<T> clazz) {
        return new JacksonSerde<>(objectMapper, objectMapper.constructType(clazz));
    }

    public static <T> Serde<T> byParameterized(
            ObjectMapper objectMapper,
            Class<?> parameterized,
            Class<?> ...parameterizedClasses
    ) {
        JavaType type = objectMapper.getTypeFactory().constructParametricType(parameterized, parameterizedClasses);
        return new JacksonSerde<>(objectMapper, type);
    }

    @Override
    @Nonnull
    public T deserialize(@Nonnull InputStream inputStream) throws IOException {
        return this.objectMapper.readValue(inputStream, this.javaType);
    }

    @Override
    public void serialize(@Nonnull T object, @Nonnull OutputStream outputStream) throws IOException {
        this.objectMapper.writeValue(outputStream, object);
    }

}
