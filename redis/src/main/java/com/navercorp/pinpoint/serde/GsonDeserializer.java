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
import com.google.gson.GsonBuilder;
import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class GsonDeserializer<T> implements Deserializer<T> {

    private final Gson gson;
    private final Type type;

    public GsonDeserializer(Type type) {
        this(createGson(), type);
    }

    static Gson createGson() {
        return new GsonBuilder().create();
    }

    GsonDeserializer(Gson gson, Type type) {
        this.gson = Objects.requireNonNull(gson, "gson");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    @NonNull
    public T deserialize(@NonNull InputStream inputStream) {
        final Reader reader = new InputStreamReader(inputStream);
        return this.gson.fromJson(reader, this.type);
    }

}
