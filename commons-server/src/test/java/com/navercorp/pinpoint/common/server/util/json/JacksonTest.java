/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util.json;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class JacksonTest {

    @Test
    void newMapper() throws JacksonException {
        ObjectMapper mapper = Jackson.newMapper();

        User user1 = new User("abc");

        String json = mapper.writeValueAsString(user1);

        User user2 = mapper.readValue(json, User.class);

        Assertions.assertThat(user1).isEqualTo(user2);
    }

    /**
     * The EclipseCollectionsModule instance is shared by every mapper
     */
    @Test
    void eclipseCollectionsModule() throws JacksonException {
        ObjectMapper[] mappers = {Jackson.newMapper(), Jackson.newMapper(), Jackson.newBuilder().build(), Jackson.newYamlMapper()};
        ImmutableIntList intList = IntLists.immutable.of(1, 2, 3);
        ImmutableList<String> list = Lists.immutable.of("a", "b");

        for (ObjectMapper mapper : mappers) {
            Assertions.assertThat(mapper.registeredModules()).hasSize(1);

            String intListJson = mapper.writeValueAsString(intList);
            Assertions.assertThat(mapper.readValue(intListJson, ImmutableIntList.class)).isEqualTo(intList);

            String listJson = mapper.writeValueAsString(list);
            ImmutableList<String> read = mapper.readValue(listJson, new TypeReference<ImmutableList<String>>() {
            });
            Assertions.assertThat(read).isEqualTo(list);
        }

        Assertions.assertThat(Jackson.newMapper().writeValueAsString(intList)).isEqualTo("[1,2,3]");
    }

    static class User {
        private final String name;

        public User(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;
            return name.equals(user.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}