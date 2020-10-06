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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navercorp.pinpoint.common.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 *
 */
@JsonDeserialize(using = FilterDescriptor.FilterDescriptorDeserializer.class)
public class FilterDescriptor {

    private final FromNode fromNode;
    private final ToNode toNode;
    private final SelfNode selfNode;
    private final ResponseTime responseTime;
    private final Option option;


    public static class FilterDescriptorDeserializer extends JsonDeserializer<FilterDescriptor> {
        @Override
        public FilterDescriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode jsonNode = p.readValueAsTree();

            FromNode fromNode = readValueAs(FromNode.class, jsonNode, p);
            ToNode toNode = readValueAs(ToNode.class, jsonNode, p);
            SelfNode selfNode = readValueAs(SelfNode.class, jsonNode, p);
            ResponseTime responseTime= readValueAs(ResponseTime.class, jsonNode, p);
            Option option = readValueAs(Option.class, jsonNode, p);
            return new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);
        }

        private <T> T readValueAs(Class<T> valueType, JsonNode jsonNode, JsonParser p) throws IOException {
            JsonParser traverse = jsonNode.traverse(p.getCodec());
            return traverse.readValueAs(valueType);
        }
    }

    public FilterDescriptor(FromNode fromNode, ToNode toNode, SelfNode selfNode, ResponseTime responseTime, Option option) {
        this.fromNode = Objects.requireNonNull(fromNode, "fromNode");
        this.toNode = Objects.requireNonNull(toNode, "toNode");
        this.selfNode = Objects.requireNonNull(selfNode, "self");
        this.responseTime = Objects.requireNonNull(responseTime, "responseTime");
        this.option = Objects.requireNonNull(option, "option");
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Node {
        private final String applicationName;
        private final String serviceType ;
        private final String agentId;

        public Node(String applicationName, String serviceType, String agentId) {
            this.applicationName = applicationName;
            this.serviceType = serviceType;
            this.agentId = agentId;
        }

        public String getApplicationName() {
            return applicationName;
        }

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
            return this.getClass().getSimpleName()  + "{" +
                    "applicationName='" + applicationName + '\'' +
                    ", serviceType='" + serviceType + '\'' +
                    ", agentId='" + agentId + '\'' +
                    '}';
        }
    }

    public static class FromNode extends Node {
        @JsonCreator
        public FromNode(@JsonProperty("fa") String applicationName,
                        @JsonProperty("fst") String serviceType,
                        @JsonProperty("fan") String agentId) {
            super(applicationName, serviceType, agentId);
        }
    }

    public static class ToNode extends Node {
        /**
         * to application
         */
        @JsonCreator
        public ToNode(@JsonProperty("ta") String applicationName,
                      @JsonProperty("tst") String serviceType,
                      @JsonProperty("tan") String agentId) {
            super(applicationName, serviceType, agentId);
        }
    }

    public static class SelfNode extends Node {
        /**
         * self application
         */
        @JsonCreator
        public SelfNode(@JsonProperty("a") String applicationName,
                      @JsonProperty("st") String serviceType,
                      @JsonProperty("an") String agentId) {
            super(applicationName, serviceType, agentId);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class ResponseTime {

        private final Long fromResponseTime;
        private final String toResponseTime;

        public ResponseTime(@JsonProperty("rf") Long fromResponseTime,
                            @JsonProperty("rt") String toResponseTime) {
            this.fromResponseTime = fromResponseTime;
            this.toResponseTime = toResponseTime;
        }

        public Long getFromResponseTime() {
            return fromResponseTime;
        }

        public Long getToResponseTime() {
            if (toResponseTime == null) {
                return null;
            } else if ("max".equals(toResponseTime)) {
                return Long.MAX_VALUE;
            } else {
                return Long.valueOf(toResponseTime);
            }
        }

        public String getRawToResponseTime() {
            return toResponseTime;
        }


        public boolean isValid() {
            return !((fromResponseTime == null && StringUtils.hasLength(toResponseTime)) || (fromResponseTime != null && StringUtils.isEmpty(toResponseTime)));
        }

        @Override
        public String toString() {
            return "ResponseTime{" +
                    "fromResponseTime=" + fromResponseTime +
                    ", toResponseTime='" + toResponseTime + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Option {

        /**
         * requested url
         */
        private String urlPattern = null;

        /**
         * include exception
         */
        private Boolean includeException = null;

        public Option(@JsonSetter(value = "url") String urlPattern,
                      @JsonSetter(value = "ie") Boolean includeException) {
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

    public FromNode getFromNode() {
        return fromNode;
    }

    public ToNode getToNode() {
        return toNode;
    }

    public SelfNode getSelfNode() {
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
        return new String(Base64.decodeBase64(urlPattern), Charsets.UTF_8);
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
