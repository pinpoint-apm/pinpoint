/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.cluster;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public enum Role {

    CALLER, CALLEE, ROUTER, UNKNOWN;

    private static final Set<Role> ROLES = EnumSet.allOf(Role.class);

    public static Role getValue(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        for (Role role : ROLES) {
            if (name.equals(role.name())) {
                return role;
            }
        }

        return UNKNOWN;
    }

}
