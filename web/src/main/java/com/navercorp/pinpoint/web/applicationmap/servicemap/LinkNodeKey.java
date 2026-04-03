/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.servicemap;

import com.navercorp.pinpoint.web.applicationmap.nodes.ServiceNodeName;
import com.navercorp.pinpoint.web.vo.Service;

public interface LinkNodeKey {

    String value();

    static LinkNodeKey ofService(Service service) {
        return new ServiceKey(service.getServiceName());
    }

    static LinkNodeKey ofNode(ServiceNodeName serviceNodeName) {
        return new NodeKey(serviceNodeName.getName());
    }

    record ServiceKey(String value) implements LinkNodeKey {
    }

    record NodeKey(String value) implements LinkNodeKey {
    }
}
