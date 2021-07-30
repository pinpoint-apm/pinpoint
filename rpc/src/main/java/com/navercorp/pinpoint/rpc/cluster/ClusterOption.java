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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.util.MapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ClusterOption {

    public static final ClusterOption DISABLE_CLUSTER_OPTION = new ClusterOption(false, "", Collections.<Role>emptyList());

    private final boolean enable;
    private final String id;
    private final List<Role> roles;

    public ClusterOption(boolean enable, String id, String role) {
        this(enable, id, Role.getValue(role));
    }

    public ClusterOption(boolean enable, String id, Role role) {
        this(enable, id, Arrays.asList(role));
    }

    public ClusterOption(ClusterOption clusterOption) {
        this(clusterOption.enable, clusterOption.id, new ArrayList<>(clusterOption.roles));
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

    public Map<String, Object> toMap() {
        if (!enable) {
            return Collections.emptyMap();
        }

        Map<String, Object> clusterProperties = new HashMap<>(2);
        clusterProperties.put("id", id);

        List<String> roleList = new ArrayList<>(roles.size());
        for (Role role : roles) {
            roleList.add(role.name());
        }
        clusterProperties.put("roles", roleList);

        return clusterProperties;
    }

    public static ClusterOption getClusterOption(Map<?, ?> handshakeResponse) {
        if (MapUtils.isEmpty(handshakeResponse)) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        }

        final Map<?, ?> cluster = (Map<?, ?>) handshakeResponse.get(ControlHandshakeResponsePacket.CLUSTER);
        if (cluster == null) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        }

        String id = MapUtils.getString(cluster, "id", "");
        List<Role> roles = getRoles(cluster.get("roles"));

        if (StringUtils.isEmpty(id)) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        } else {
            return new ClusterOption(true, id, roles);
        }
    }

    private static List<Role> getRoles(Object roleNames) {
        if (!(roleNames instanceof List)) {
            return new ArrayList<>();
        }

        final List<Role> roles = new ArrayList<>();
        final List<Object> list = (List<Object>) roleNames;
        for (Object roleName : list) {
            if (roleName instanceof String && StringUtils.hasLength((String) roleName)) {
                roles.add(Role.getValue((String) roleName));
            }
        }
        return roles;
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

    public static ClusterOption copy(ClusterOption clusterOption) {
        return new ClusterOption(clusterOption.enable, clusterOption.id, clusterOption.roles);
    }
}
