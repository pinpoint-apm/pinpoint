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
package com.navercorp.pinpoint.serde;

import com.google.gson.Gson;
import org.springframework.core.serializer.Serializer;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class GsonSerializer<T> implements Serializer<T> {

    private final Gson gson;
    private final Type type;

    public GsonSerializer(Type type) {
        this(GsonDeserializer.createGson(), type);
    }

    GsonSerializer(Gson gson, Type type) {
        this.gson = Objects.requireNonNull(gson, "gson");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public void serialize(@NonNull T item, @NonNull OutputStream outputStream) throws IOException {
        final Writer writer = new OutputStreamWriter(outputStream);
        this.gson.toJson(item, this.type, writer);
        writer.flush();
    }

}
