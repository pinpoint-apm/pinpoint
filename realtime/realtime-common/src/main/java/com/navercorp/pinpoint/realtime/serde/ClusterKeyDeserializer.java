/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;

import java.io.IOException;

/**
 * @author youngjin.kim2
 */
public class ClusterKeyDeserializer extends StdDeserializer<ClusterKey> {

    public ClusterKeyDeserializer() {
        super(ClusterKey.class);
    }

    @Override
    public ClusterKey deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        TreeNode root = parser.getCodec().readTree(parser);
        String applicationName = ((TextNode) root.get("applicationName")).asText();
        String agentId = ((TextNode) root.get("agentId")).asText();
        long startTimestamp = ((LongNode) root.get("startTimestamp")).asLong();
        return new ClusterKey(applicationName, agentId, startTimestamp);
    }

}
