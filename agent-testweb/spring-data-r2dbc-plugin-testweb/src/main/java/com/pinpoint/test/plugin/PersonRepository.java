/*
 * Copyright 2022 NAVER Corp.
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

package com.pinpoint.test.plugin;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import static org.springframework.data.relational.core.query.Criteria.where;

public class PersonRepository {

    private final R2dbcEntityTemplate template;

    public PersonRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }

    public Flux<Person> findByFirstNameContains(String name) {
        return this.template.select(Person.class)
                .matching(Query.query(where("firstName").like("%" + name + "%")).limit(10).offset(0))
                .all();
    }

    public Mono<Long> countByFirstNameContains(String name) {
        return this.template.count(Query.query(where("firstName").like("%" + name + "%")), Person.class);
    }


    public Flux<Person> findAll() {
        return this.template.select(Person.class).all();
    }

    public Mono<Long> count() {
        return this.template.count(Query.empty(), Person.class);
    }

    public Mono<String> save(Person p) {
        return this.template.insert(Person.class)
                .using(p)
                .map(person -> person.getFirstName());
    }

    public Mono<Integer> update(Person p) {
/*
        return this.template.update(Post.class)
                .matching(Query.query(where("id").is(p.getId())))
                .apply(Update.update("title", p.getTitle())
                        .set("content", p.getContent())
                        .set("status", p.getStatus())
                        .set("metadata", p.getMetadata()));
*/
        return this.template.update(
                Query.query(where("id").is(p.getFirstName())),
                Update.update("firstName", p.getFirstName())
                        .set("lastName", p.getLastName())
                        .set("age", p.getAge()),
                Person.class
        );
    }

    public Mono<Integer> deleteById(int id) {
        return this.template.delete(Query.query(where("id").is(id)), Person.class);
    }
}
