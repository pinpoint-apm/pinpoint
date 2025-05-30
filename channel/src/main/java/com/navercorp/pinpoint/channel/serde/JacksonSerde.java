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

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class JacksonSerde<T> implements Serde<T> {

    private final ObjectReader reader;
    private final ObjectWriter writer;

    public JacksonSerde(ObjectReader reader, ObjectWriter writer) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    @Override
    @Nonnull
    public T deserialize(@Nonnull InputStream inputStream) throws IOException {
        return reader.readValue(inputStream);
    }

    @Override
    public void serialize(@Nonnull T object, @Nonnull OutputStream outputStream) throws IOException {
        this.writer.writeValue(outputStream, object);
    }

}
