/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

/**
 * @author Taejin Koo
 */
public class ZookeeperConstants {


    public static final String PATH_SEPARATOR = "/";

    public static final String DEFAULT_CLUSTER_ZNODE_ROOT_PATH = "/pinpoint-cluster";

    public static final String WEB_LEAF_PATH = "web";
    public static final String COLLECTOR_LEAF_PATH = "collector";
    public static final String FLINK_LEAF_PATH = "flink";

}
