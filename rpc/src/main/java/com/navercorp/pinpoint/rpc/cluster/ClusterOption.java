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

import java.util.*;

/**
 * @author Taejin Koo
 */
public class ClusterOption {

    public static final ClusterOption DISABLE_CLUSTER_OPTION = new ClusterOption(false, "", Collections.EMPTY_LIST);

    private final boolean enable;
    private final String id;
    private final List<Role> roles;

    public ClusterOption(boolean enable, String id, String role) {
        this(enable, id, Role.getValue(role));
    }

    public ClusterOption(boolean enable, String id, Role role) {
        this(enable, id, Arrays.asList(role));
    }

    public ClusterOption(boolean enable, String id, List<Role> roles) {
        this.enable = enable;
        this.id = id;
        this.roles = roles;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getId() {
        return id;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Map<String, Object> getProperties() {
        if (!enable) {
            return Collections.emptyMap();
        }

        Map<String, Object> clusterProperties = new HashMap<String, Object>(2);
        clusterProperties.put("id", id);

        List<String> roleList = new ArrayList<String>(roles.size());
        for (Role role : roles) {
            roleList.add(role.name());
        }
        clusterProperties.put("roles", roleList);

        return clusterProperties;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClusterOption{");
        sb.append("enable=").append(enable);
        sb.append(", id='").append(id).append('\'');
        sb.append(", roles=").append(roles);
        sb.append('}');
        return sb.toString();
    }
}
