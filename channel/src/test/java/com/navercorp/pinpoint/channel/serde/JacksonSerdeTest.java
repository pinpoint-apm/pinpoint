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
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class JacksonSerdeTest {

    @Test
    @DisplayName("Serialize -> Deserialize should be identity")
    public void testIdentity() throws IOException {
        Random random = new Random();
        int p0 = random.nextInt();
        int p1 = random.nextInt();

        Foo<Foo<String>> target = makeFooFooString(p0, p1, "Hello");

        Serde<Foo<Foo<String>>> serde = getSerde();
        byte[] bytes = serde.serializeToByteArray(target);
        Foo<Foo<String>> result = serde.deserializeFromByteArray(bytes);

        assertThat(result.getFoo()).isEqualTo(p0);
        assertThat(result.getBar().getFoo()).isEqualTo(p1);
        assertThat(result.getBar().getBar()).isEqualTo("Hello");
    }

    private static Foo<Foo<String>> makeFooFooString(int p0, int p1, String p2) {
        Foo<Foo<String>> target = new Foo<>();
        target.setFoo(p0);
        target.setBar(new Foo<>());
        target.getBar().setFoo(p1);
        target.getBar().setBar(p2);
        return target;
    }

    private static Serde<Foo<Foo<String>>> getSerde() {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType t1 = typeFactory.constructParametricType(Foo.class, String.class);
        JavaType t2 = typeFactory.constructParametricType(Foo.class, t1);
        return new JacksonSerde<>(objectMapper, t2);
    }

    private static class Foo<T> {
        private int foo;
        private T bar;

        public int getFoo() {
            return foo;
        }

        public void setFoo(int foo) {
            this.foo = foo;
        }

        public T getBar() {
            return bar;
        }

        public void setBar(T bar) {
            this.bar = bar;
        }
    }


}
