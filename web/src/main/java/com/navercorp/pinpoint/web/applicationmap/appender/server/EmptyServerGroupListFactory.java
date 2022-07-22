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

package com.navercorp.pinpoint.web.applicationmap.appender.server;

import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;

import java.time.Instant;

/**
 * @author HyunGil Jeong
 */
public class EmptyServerGroupListFactory implements ServerGroupListFactory {

    @Override
    public ServerGroupList createWasNodeInstanceList(Node wasNode, Instant timestamp) {
        return ServerGroupList.empty();
    }

    @Override
    public ServerGroupList createTerminalNodeInstanceList(Node terminalNode, LinkDataDuplexMap linkDataDuplexMap) {
        return ServerGroupList.empty();
    }

    @Override
    public ServerGroupList createQueueNodeInstanceList(Node queueNode, LinkDataDuplexMap linkDataDuplexMap) {
        return ServerGroupList.empty();
    }

    @Override
    public ServerGroupList createUserNodeInstanceList() {
        return ServerGroupList.empty();
    }

    @Override
    public ServerGroupList createEmptyNodeInstanceList() {
        return ServerGroupList.empty();
    }
}
