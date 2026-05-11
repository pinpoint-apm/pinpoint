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
package com.navercorp.pinpoint.web.trace.callstacks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.trace.Api;
import com.navercorp.pinpoint.common.server.trace.ApiParser;
import com.navercorp.pinpoint.common.server.trace.ApiParserProvider;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.util.AnnotationUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.component.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.trace.span.Align;
import com.navercorp.pinpoint.web.trace.span.CallTreeNode;
import com.navercorp.pinpoint.web.util.OtelLinkValue;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author minwoo.jung
 */
public class RecordFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // spans with id = 0 are regarded as root - start at 1
    private int idGen = 1;
    private final AnnotationKeyMatcherService annotationKeyMatcherService;
    private final ServiceTypeRegistryService registry;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    private final AnnotationRecordFormatter annotationRecordFormatter;

    private final ApiParserProvider apiParserProvider;

    public RecordFactory(final AnnotationKeyMatcherService annotationKeyMatcherService,
                         final ServiceTypeRegistryService registry,
                         final AnnotationKeyRegistryService annotationKeyRegistryService,
                         final AnnotationRecordFormatter annotationRecordFormatter,
                         final ApiParserProvider apiParserProvider) {
        this.annotationKeyMatcherService = Objects.requireNonNull(annotationKeyMatcherService, "annotationKeyMatcherService");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.annotationKeyRegistryService = Objects.requireNonNull(annotationKeyRegistryService, "annotationKeyRegistryService");

        this.annotationRecordFormatter = Objects.requireNonNull(annotationRecordFormatter, "annotationRecordFormatter");
        this.apiParserProvider = Objects.requireNonNull(apiParserProvider, "apiParserRegistry");
    }

    public Record get(final CallTreeNode node) {
        final Align align = node.getAlign();
        align.setId(getNextId());

        final int parentId = getParentId(node);
        Api api = getApi(align);
        final String argument = getArgument(align);
        final Record record = new DefaultRecord(align.getDepth(),
                align.getId(),
                parentId,
                true,
                api.getMethod(),
                argument,
                align.getStartTime(),
                align.getElapsed(),
                align.getGap(),
                align.getAgentId(),
                align.getAgentName(),
                align.getApplicationName(),
                registry.findServiceType(align.getServiceType()),
                align.getDestinationId(),
                align.hasChild(),
                false,
                align.getTransactionId(),
                align.getSpanId(),
                align.getExecutionMilliseconds(),
                api.getMethodTypeEnum(),
                true,
                api.getLineNumber(),
                api.getLocation());
        record.setSimpleClassName(api.getClassName());
        record.setFullApiDescription(api.getDescription());

        return record;
    }

    private String getArgument(final Align align) {
        final String rpc = align.getRpc();
        if (rpc != null) {
            return rpc;
        }

        return getDisplayArgument(align);
    }

    private String getDisplayArgument(Align align) {
        final AnnotationBo displayArgument = getDisplayArgument0(align.getServiceType(), align.getAnnotationBoList());
        if (displayArgument == null) {
            return "";
        }

        final AnnotationKey key = findAnnotationKey(displayArgument.getKey());
        return this.annotationRecordFormatter.formatArguments(key, displayArgument, align);
    }

    private AnnotationBo getDisplayArgument0(final int serviceType, final List<AnnotationBo> annotationBoList) {
        if (annotationBoList == null) {
            return null;
        }

        final AnnotationKeyMatcher matcher = annotationKeyMatcherService.findAnnotationKeyMatcher(serviceType);
        if (matcher == null) {
            return null;
        }

        for (AnnotationBo annotation : annotationBoList) {
            int key = annotation.getKey();

            if (matcher.matches(key)) {
                return annotation;
            }
        }
        return null;
    }

    public Record getFilteredRecord(final CallTreeNode node, String apiTitle) {
        final Align align = node.getAlign();
        align.setId(getNextId());

        final int parentId = getParentId(node);
//        Api api = getApi(align);

        final Record record = new DefaultRecord(align.getDepth(),
                align.getId(),
                parentId,
                true,
                apiTitle,
                "",
                align.getStartTime(),
                align.getElapsed(),
                align.getGap(),
                "UNKNOWN",
                align.getAgentName(),
                align.getApplicationName(),
                ServiceType.UNKNOWN,
                "",
                false,
                false,
                align.getTransactionId(),
                align.getSpanId(),
                align.getExecutionMilliseconds(),
                MethodTypeEnum.DEFAULT,
                false,
                0,
                "");

        return record;
    }

    public Record getException(final int depth, final int parentId, final Align align) {
        if (!align.hasException()) {
            return null;
        }
        return new ExceptionRecord(
                depth, getNextId(), parentId, align,
                registry.findServiceType(
                        align.getApplicationServiceType()
                )
        );
    }

    public List<Record> getAnnotations(final int depth, final int parentId, Align align) {
        List<Record> list = new ArrayList<>();
        for (AnnotationBo annotation : align.getAnnotationBoList()) {
            final AnnotationKey key = findAnnotationKey(annotation.getKey());
            if (key.isViewInRecordSet()) {
                final String title = this.annotationRecordFormatter.formatTitle(key, annotation, align);
                String arguments = this.annotationRecordFormatter.formatArguments(key, annotation, align);
                if (annotation.getKey() == AnnotationKey.OPENTELEMETRY_LINK.getCode()) {
                    arguments = augmentOtelLink(arguments, align);
                }
                final Record record = new AnnotationRecord(
                        depth, getNextId(), parentId, title, arguments, annotation.isAuthorized()
                );
                list.add(record);
            }
        }

        return list;
    }

    private String augmentOtelLink(String arguments, Align align) {
        // traceId/spanId in the raw annotation point to the OTel Link target (upstream).
        // Augment with linkTraceId/linkSpanId of the current (downstream) span where the Link annotation lives.
        final OtelLinkValue value = OtelLinkValue.parse(arguments);
        if (value == null) {
            return arguments;
        }
        final ServerTraceId linkTraceId;
        try {
            linkTraceId = ServerTraceId.of(align.getTransactionId());
        } catch (RuntimeException e) {
            return arguments;
        }
        return value.withDownstream(linkTraceId, align.getSpanId(), align.getCollectorAcceptTime())
                .toJson();
    }

    public Record getAttribute(final int depth, final int parentId, Align align) {
        List<AttributeBo> attributeBoList = align.getAttributeBoList();
        if (attributeBoList == null || attributeBoList.isEmpty()) {
            return null;
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (AttributeBo attr : attributeBoList) {
            map.put(attr.getKey(), toPlainObject(attr.getValue()));
        }
        try {
            String arguments = OBJECT_MAPPER.writeValueAsString(map);
            return new AnnotationRecord(depth, getNextId(), parentId, "Attribute", arguments, true);
        } catch (JsonProcessingException e) {
            return new AnnotationRecord(depth, getNextId(), parentId, "Attribute", "json processing error", true);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object toPlainObject(AttributeValue value) {
        if (value == null) {
            return null;
        }
        return switch (value.getType()) {
            case STRING, BOOLEAN, LONG, DOUBLE -> value.getValue();
            case BYTES -> Base64.getEncoder().encodeToString((byte[]) value.getValue());
            case ARRAY -> {
                List<AttributeValue> list = (List<AttributeValue>) value.getValue();
                yield list.stream().map(RecordFactory::toPlainObject).toList();
            }
            case KEY_VALUE_LIST -> {
                List<AttributeKeyValue> kvList = (List<AttributeKeyValue>) value.getValue();
                LinkedHashMap<String, Object> kvMap = new LinkedHashMap<>();
                for (AttributeKeyValue kv : kvList) {
                    kvMap.put(kv.getKey(), toPlainObject(kv.getValue()));
                }
                yield kvMap;
            }
        };
    }

    public Record getParameter(final int depth, final int parentId, final String method, final String argument) {
        return new ParameterRecord(depth, getNextId(), parentId, method, argument);
    }

    public Record getErrorCategory(final int depth, final int parentId, final Set<ErrorCategory> categories) {
        String argument = categories.stream().map(Enum::name).collect(Collectors.joining(", "));
        return ParameterRecord.errorRecord(depth, getNextId(), parentId, argument);
    }

    int getParentId(final CallTreeNode node) {
        final CallTreeNode parent = node.getParent();
        if (parent == null) {
            if (!node.getAlign().isSpan()) {
                throw new IllegalStateException("parent is null. node=" + node);
            }

            return 0;
        }

        return parent.getAlign().getId();
    }

    private Api getApi(final Align align) {
        final AnnotationBo annotation = AnnotationUtils.findAnnotation(align.getAnnotationBoList(), AnnotationKey.API_METADATA);
        if (annotation != null) {
            final ApiMetaDataBo apiMetaData = (ApiMetaDataBo) annotation.getValue();
            String apiInfo = apiMetaData.getDescription();

            if (apiMetaData.getMethodTypeEnum() == MethodTypeEnum.DEFAULT) {
                ApiParser parser = apiParserProvider.getParser();
                return parser.parse(apiMetaData);
            }
            // parse error
            return new Api.Builder(apiInfo, "", apiInfo, apiMetaData.getMethodTypeEnum())
                    .setLineNumber(apiMetaData.getLineNumber())
                    .setLocation(apiMetaData.getLocation())
                    .build();
        } else {
            AnnotationKey apiMetaDataError = getApiMetaDataError(align.getAnnotationBoList());

            return new Api.Builder(apiMetaDataError.getName(), "", "", MethodTypeEnum.DEFAULT).build();
        }
    }


    public AnnotationKey getApiMetaDataError(List<AnnotationBo> annotationBoList) {
        for (AnnotationBo bo : annotationBoList) {
            AnnotationKey apiErrorCode = annotationKeyRegistryService.findApiErrorCode(bo.getKey());
            if (apiErrorCode != null) {
                return apiErrorCode;
            }
        }
        // could not find a more specific error - returns generalized error
        return AnnotationKey.ERROR_API_METADATA_ERROR;
    }

    private AnnotationKey findAnnotationKey(int key) {
        return annotationKeyRegistryService.findAnnotationKey(key);
    }

    private int getNextId() {
        return idGen++;
    }

}