/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument;

import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMMethodInsnNodeRemapper {
    private List<Filter> filters = new ArrayList<Filter>();
    private String owner;
    private String name;
    private String desc;

    public void addFilter(final String owner, final String name, final String desc) {
        this.filters.add(new Filter(owner, name, desc));
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    private String mapOwner(final String owner) {
        return this.owner != null ? this.owner : owner;
    }

    public void setName(final String name) {
        this.name = name;
    }

    private String mapName(final String name) {
        return this.name != null ? this.name : name;
    }

    public void setDesc(final String desc) {
        this.desc = desc;
    }

    private String mapDesc(final String desc) {
        return this.desc != null ? this.desc : desc;
    }

    public void mapping(final MethodInsnNode methodInsnNode) {
        for (Filter filter : this.filters) {
            if (filter.accept(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc)) {
                methodInsnNode.owner = mapOwner(methodInsnNode.owner);
                methodInsnNode.name = mapName(methodInsnNode.name);
                methodInsnNode.desc = mapDesc(methodInsnNode.desc);
            }
        }
    }

    private class Filter {
        private String owner;
        private String name;
        private String desc;

        public Filter(final String owner, final String name, final String desc) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        public boolean accept(final String owner, final String name, final String desc) {
            if (this.owner != null && !this.owner.equals(owner)) {
                return false;
            }

            if (this.name != null && !this.name.equals(name)) {
                return false;
            }

            if (this.desc != null && !this.desc.equals(desc)) {
                return false;
            }

            return true;
        }
    }
}