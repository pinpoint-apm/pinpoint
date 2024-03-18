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
package com.navercorp.pinpoint.bootstrap.java9.module;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.util.Optional;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class SingleModuleFinder implements ModuleFinder {
    private final ModuleReference target;

    public SingleModuleFinder(ModuleDescriptor descriptor, URI uri) {
        this.target = new ModuleReference(descriptor, uri) {
            @Override
            public ModuleReader open() {
                throw new RuntimeException("open must not be called");
            }
        };
    }

    @Override
    public Optional<ModuleReference> find(String name) {
        if (target.descriptor().name().equals(name)) {
            return Optional.of(target);
        }
        return Optional.empty();
    }

    @Override
    public Set<ModuleReference> findAll() {
        return Set.of(target);
    }
}
