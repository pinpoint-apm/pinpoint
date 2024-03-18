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

import com.navercorp.pinpoint.common.util.StringUtils;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMMethodInsnNodeRemapper {
    private final Filter[] filters;
    private final String owner;
    private final String name;
    private final String desc;

    private ASMMethodInsnNodeRemapper(Filter[] filters, String owner, String name, String desc) {
        this.filters = filters;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    private String mapOwner(final String ownerClassInternalName) {
        return StringUtils.defaultString(this.owner, ownerClassInternalName);
    }

    private String mapName(final String name) {
        return StringUtils.defaultString(this.name, name);
    }


    private String mapDesc(final String desc) {
        return StringUtils.defaultString(this.desc, desc);
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

    public static class Builder {
        private final List<Filter> filters = new ArrayList<>();
        private String owner;
        private String name;
        private String desc;

        public void addFilter(final String ownerClassInternalName, final String name, final String desc) {
            this.filters.add(new Filter(ownerClassInternalName, name, desc));
        }

        public void setOwner(final String ownerClassInternalName) {
            this.owner = ownerClassInternalName;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setDesc(final String desc) {
            this.desc = desc;
        }

        public ASMMethodInsnNodeRemapper build() {
            Filter[] copyFilter = this.filters.toArray(new Filter[0]);
            return new ASMMethodInsnNodeRemapper(copyFilter, owner, name, desc);
        }
    }

    private static class Filter {
        private final String owner;
        private final String name;
        private final String desc;

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