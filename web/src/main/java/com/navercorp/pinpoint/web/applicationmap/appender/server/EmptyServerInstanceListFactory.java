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
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;

import java.time.Instant;

/**
 * @author HyunGil Jeong
 */
public class EmptyServerInstanceListFactory implements ServerInstanceListFactory {

    @Override
    public ServerInstanceList createWasNodeInstanceList(Node wasNode, Instant timestamp) {
        return new ServerInstanceList();
    }

    @Override
    public ServerInstanceList createTerminalNodeInstanceList(Node terminalNode, LinkDataDuplexMap linkDataDuplexMap) {
        return new ServerInstanceList();
    }

    @Override
    public ServerInstanceList createQueueNodeInstanceList(Node queueNode, LinkDataDuplexMap linkDataDuplexMap) {
        return new ServerInstanceList();
    }

    @Override
    public ServerInstanceList createUserNodeInstanceList() {
        return new ServerInstanceList();
    }

    @Override
    public ServerInstanceList createEmptyNodeInstanceList() {
        return new ServerInstanceList();
    }
}
