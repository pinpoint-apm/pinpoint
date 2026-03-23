/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 *
 */
@JsonDeserialize(using = FilterDescriptor.FilterDescriptorDeserializer.class)
public class FilterDescriptor {

    private final Node fromNode;
    private final Node toNode;
    private final Node selfNode;
    private final ResponseTime responseTime;
    private final Option option;


    public static class FilterDescriptorDeserializer extends JsonDeserializer<FilterDescriptor> {
        @Override
        public FilterDescriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode jsonNode = p.readValueAsTree();

            Node fromNode = readNode(jsonNode, Node.NodeType.FROM, "fa", "fst", "fan");
            Node toNode = readNode(jsonNode, Node.NodeType.TO, "ta", "tst", "tan");
            Node selfNode = readNode(jsonNode, Node.NodeType.SELF, "a", "st", "an");

            ResponseTime responseTime = ResponseTime.of(JsonNodeUtils.longValue(jsonNode, "rf"), JsonNodeUtils.textValue(jsonNode, "rt"));

            Option option = new Option(JsonNodeUtils.textValue(jsonNode, "url"), JsonNodeUtils.booleanValue(jsonNode, "ie"));
            return new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);
        }

        private static Node readNode(JsonNode jsonNode, Node.NodeType type, String applicationNameField, String serviceTypeField, String agentIdField) {
            String applicationName = JsonNodeUtils.textValue(jsonNode, applicationNameField);
            String serviceType = JsonNodeUtils.textValue(jsonNode, serviceTypeField);
            String agentId = JsonNodeUtils.textValue(jsonNode, agentIdField);
            return new Node(type, applicationName, serviceType, agentId);
        }
    }

    public FilterDescriptor(Node fromNode, Node toNode, Node selfNode, ResponseTime responseTime, Option option) {
        this.fromNode = Objects.requireNonNull(fromNode, "fromNode");
        this.toNode = Objects.requireNonNull(toNode, "toNode");
        this.selfNode = Objects.requireNonNull(selfNode, "self");
        this.responseTime = Objects.requireNonNull(responseTime, "responseTime");
        this.option = Objects.requireNonNull(option, "option");
    }

    public static class Node {
        private final NodeType type;

        enum NodeType {
            FROM, TO, SELF
        }

        @Nullable
        private final String applicationName;
        @Nullable
        private final String serviceType ;
        private final String agentId;

        public Node(NodeType type, String applicationName, String serviceType, String agentId) {
            this.type = Objects.requireNonNull(type, "type");
            this.applicationName = applicationName;
            this.serviceType = serviceType;
            this.agentId = agentId;
        }

        @Nullable
        public String getApplicationName() {
            return applicationName;
        }

        @Nullable
        public String getServiceType() {
            return serviceType;
        }

        public String getAgentId() {
            return agentId;
        }

        public boolean isValid() {
            return StringUtils.hasLength(applicationName) && StringUtils.hasLength(serviceType);
        }

        @Override
        public String toString() {
            return type  + "{" +
                    "applicationName='" + applicationName + '\'' +
                    ", serviceType='" + serviceType + '\'' +
                    ", agentId='" + agentId + '\'' +
                    '}';
        }
    }


    public static class ResponseTime {

        private final Long fromResponseTime;
        private final Long toResponseTime;

        public static ResponseTime of(Long fromResponseTime, String rawToResponseTime) {
            Long toResponseTime = parseLongResponseTime(rawToResponseTime);
            return new ResponseTime(fromResponseTime, toResponseTime);
        }

        public ResponseTime(Long fromResponseTime, Long toResponseTime) {
            this.fromResponseTime = fromResponseTime;
            this.toResponseTime = toResponseTime;
        }

        public Long getFromResponseTime() {
            return fromResponseTime;
        }

        public Long getToResponseTime() {
            return this.toResponseTime;
        }

        private static Long parseLongResponseTime(String responseTime) {
            if (responseTime == null) {
                return null;
            } else if ("max".equals(responseTime)) {
                return Long.MAX_VALUE;
            } else {
                return Long.parseLong(responseTime);
            }
        }


        public boolean isValid() {
            return !((fromResponseTime == null && toResponseTime != null) || (fromResponseTime != null && toResponseTime == null));
        }

        @Override
        public String toString() {
            return "ResponseTime{" +
                    "fromResponseTime=" + fromResponseTime +
                    ", toResponseTime='" + toResponseTime + '\'' +
                    '}';
        }
    }

    public static class Option {

        /**
         * requested url
         */
        private String urlPattern;

        /**
         * include exception
         */
        private Boolean includeException;

        public Option(String urlPattern, Boolean includeException) {
            this.urlPattern = decodeBase64(urlPattern);
            this.includeException = includeException;
        }

        public Boolean getIncludeException() {
            return includeException;
        }


        public String getUrlPattern() {
            return urlPattern;
        }

        @Override
        public String toString() {
            return "Option{" +
                    "urlPattern='" + urlPattern + '\'' +
                    ", includeException=" + includeException +
                    '}';
        }
    }


    public boolean isValid() {
        return isValidNodeInfo() && responseTime.isValid();
    }

    public boolean isValidNodeInfo() {
        return this.fromNode.isValid() || this.toNode.isValid() || this.selfNode.isValid();
    }

    public Node getFromNode() {
        return fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

    public Node getSelfNode() {
        return selfNode;
    }

    public ResponseTime getResponseTime() {
        return responseTime;
    }

    public Option getOption() {
        return option;
    }

    private static String decodeBase64(String urlPattern) {
        if (urlPattern == null) {
            return null;
        }
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        byte[] decode = urlDecoder.decode(urlPattern);
        return new String(decode, StandardCharsets.ISO_8859_1);
    }


    @Override
    public String toString() {
        return "FilterDescriptor{" +
                "fromNode=" + fromNode +
                ", toNode=" + toNode +
                ", selfNode=" + selfNode +
                ", responseTime=" + responseTime +
                ", option=" + option +
                '}';
    }
}
