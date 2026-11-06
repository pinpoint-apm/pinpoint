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

package com.navercorp.pinpoint.web.install.model;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.node.ArrayNode;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Taejin Koo
 */
@JsonDeserialize(using = GithubAgentDownloadInfo.Deserializer.class)
public class GithubAgentDownloadInfo extends AgentDownloadInfo {

    public GithubAgentDownloadInfo() {
        super();
    }

    public GithubAgentDownloadInfo(String version, String downloadUrl) {
        super(version, downloadUrl);
    }

    public static class Deserializer extends ValueDeserializer<GithubAgentDownloadInfo> {
        private static final String VERSION_PREFIX = "v";

        @Override
        public GithubAgentDownloadInfo deserialize(JsonParser jp, DeserializationContext ctxt) {

            JsonNode jsonNode = jp.readValueAsTree();

            if (!jsonNode.isObject()) {
                return null;
            }

            JsonNode tagNameNode = jsonNode.get("tag_name");
            if (!tagNameNode.isString()) {
                return null;
            }

            String tagName = tagNameNode.asString();
            if (StringUtils.isEmpty(tagName)) {
                return null;
            }

            JsonNode assets1 = jsonNode.get("assets");
            if (!assets1.isArray()) {
                return null;
            }
            ArrayNode arrayNode = (ArrayNode) assets1;

            String downloadUrl = getDownloadUrl(arrayNode);
            if (StringUtils.isEmpty(downloadUrl)) {
                return null;
            }
            tagName = cleanupVersionPrefix(tagName);
            return new GithubAgentDownloadInfo(tagName, downloadUrl);
        }

        private String cleanupVersionPrefix(String version) {
            if (version == null) {
                return null;
            }
            final boolean prefix = version.startsWith(VERSION_PREFIX);
            if (prefix) {
                return version.substring(VERSION_PREFIX.length());
            }
            return version;
        }


        private String getDownloadUrl(ArrayNode assetsNode) {
            for (int i = 0; i < assetsNode.size(); i++) {
                JsonNode jsonNode = assetsNode.get(i);

                if (!jsonNode.isObject()) {
                    continue;
                }

                JsonNode nameNode = jsonNode.get("name");
                if (!nameNode.isString()) {
                    continue;
                }

                String name = nameNode.asString();
                if (!name.contains("pinpoint-agent")) {
                    continue;
                }

                JsonNode downloadNode = jsonNode.get("browser_download_url");
                if (!downloadNode.isString()) {
                    continue;
                }

                String download = downloadNode.asString();
                if (StringUtils.hasLength(download)) {
                    return download;
                }
            }
            return null;
        }

    }

}
