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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

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

    public static class Deserializer extends JsonDeserializer<GithubAgentDownloadInfo> {

        @Override
        public GithubAgentDownloadInfo deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonNode jsonNode = mapper.readTree(jp);

            if (!jsonNode.isObject()) {
                return null;
            }

            JsonNode tagNameNode = jsonNode.get("tag_name");
            if (!tagNameNode.isTextual()) {
                return null;
            }

            String tagName = tagNameNode.asText();
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


            return new GithubAgentDownloadInfo(tagName, downloadUrl);
        }


        private String getDownloadUrl(ArrayNode assetsNode) {
            for (int i = 0; i < assetsNode.size(); i++) {
                JsonNode jsonNode = assetsNode.get(i);

                if (!jsonNode.isObject()) {
                    continue;
                }

                JsonNode nameNode = jsonNode.get("name");
                if (!nameNode.isTextual()) {
                    continue;
                }

                String name = nameNode.asText();
                if (!name.contains("pinpoint-agent")) {
                    continue;
                }

                JsonNode downloadNode = jsonNode.get("browser_download_url");
                if (!downloadNode.isTextual()) {
                    continue;
                }

                String download = downloadNode.asText();
                if (!StringUtils.isEmpty(download)) {
                    return download;
                }
            }
            return null;
        }

    }

}
