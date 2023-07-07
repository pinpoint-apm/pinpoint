/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class JacksonParameterNamesModuleTest {

    @Test
    void parameterNamesModule_args1() throws JsonProcessingException {
        ObjectMapper mapper = Jackson.newMapper();

        Person1 user = new Person1("user");
        String json = mapper.writeValueAsString(user);

        Person1 user2 = mapper.readValue(json, Person1.class);

        Assertions.assertThat(user).isEqualTo(user2);
    }


    @Test
    void parameterNamesModule_arg2() throws JsonProcessingException {
        ObjectMapper mapper = Jackson.newMapper();

        Person2 user = new Person2("user", 10);
        String json = mapper.writeValueAsString(user);

        Person2 user2 = mapper.readValue(json, Person2.class);

        Assertions.assertThat(user).isEqualTo(user2);
    }


    static class Person1 {

        private final String name;

        @JsonCreator
        public Person1(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Person1 person1 = (Person1) o;

            return name.equals(person1.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }


    static class Person2 {

        private final String name;
        private final int age;


        public Person2(String name, int age) {
            this.name = Objects.requireNonNull(name, "name");
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Person2 person2 = (Person2) o;

            if (age != person2.age) return false;
            return name.equals(person2.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + age;
            return result;
        }
    }

}
