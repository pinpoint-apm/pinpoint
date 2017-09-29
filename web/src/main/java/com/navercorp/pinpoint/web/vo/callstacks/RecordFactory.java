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
package com.navercorp.pinpoint.web.vo.callstacks;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.server.util.AnnotationUtils;
import com.navercorp.pinpoint.common.util.ApiDescription;
import com.navercorp.pinpoint.common.server.util.ApiDescriptionParser;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author minwoo.jung
 */
public class RecordFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // spans with id = 0 are regarded as root - start at 1
    private int idGen = 1;
    private ServiceTypeRegistryService registry;
    private AnnotationKeyRegistryService annotationKeyRegistryService;
    private final ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();
    private final AnnotationRecordFormatter annotationRecordFormatter;

    public RecordFactory(final ServiceTypeRegistryService registry, final AnnotationKeyRegistryService annotationKeyRegistryService, final StringMetaDataDao stringMetaDataDao) {
        this.registry = registry;
        this.annotationKeyRegistryService = annotationKeyRegistryService;
        this.annotationRecordFormatter = new AnnotationRecordFormatter(stringMetaDataDao);
    }

    public Record get(final CallTreeNode node, final String argument) {
        final SpanAlign align = node.getValue();
        align.setId(getNextId());

        final int parentId = getParentId(node);
        Api api = getApi(align);

        final Record record = new DefaultRecord(align.getDepth(),
                align.getId(),
                parentId,
                true,
                api.getTitle(),
                argument,
                align.getStartTime(),
                align.getElapsed(),
                align.getGap(),
                align.getAgentId(),
                align.getApplicationId(),
                registry.findServiceType(align.getServiceType()),
                align.getDestinationId(),
                align.hasChild(),
                false,
                align.getTransactionId(),
                align.getSpanId(),
                align.getExecutionMilliseconds(),
                api.getMethodTypeEnum(),
                true);
        record.setSimpleClassName(api.getClassName());
        record.setFullApiDescription(api.getDescription());

        return record;
    }

    public Record getFilteredRecord(final CallTreeNode node, String apiTitle) {
        final SpanAlign align = node.getValue();
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
                align.getApplicationId(),
                ServiceType.UNKNOWN,
                "",
                false,
                false,
                align.getTransactionId(),
                align.getSpanId(),
                align.getExecutionMilliseconds(),
                MethodTypeEnum.DEFAULT,
                false);

        return record;
    }

    public Record getException(final int depth, final int parentId, final SpanAlign align) {
        if (!align.hasException()) {
            return null;
        }
        return new ExceptionRecord(depth, getNextId(), parentId, align);
    }

    public List<Record> getAnnotations(final int depth, final int parentId, SpanAlign align) {
        List<Record> list = new ArrayList<>();
        for (AnnotationBo annotation : align.getAnnotationBoList()) {
            final AnnotationKey key = findAnnotationKey(annotation.getKey());
            if (key.isViewInRecordSet()) {
                final String title = this.annotationRecordFormatter.formatTitle(key, annotation, align);
                final String arguments = this.annotationRecordFormatter.formatArguments(key, annotation, align);
                final Record record = new AnnotationRecord(depth, getNextId(), parentId, title, arguments, annotation.isAuthorized());
                list.add(record);
            }
        }

        return list;
    }

    public Record getParameter(final int depth, final int parentId, final String method, final String argument) {
        return new ParameterRecord(depth, getNextId(), parentId, method, argument);
    }

    int getParentId(final CallTreeNode node) {
        final CallTreeNode parent = node.getParent();
        if (parent == null) {
            if (!node.getValue().isSpan()) {
                throw new IllegalStateException("parent is null. node=" + node);
            }

            return 0;
        }

        return parent.getValue().getId();
    }

    private Api getApi(final SpanAlign align) {

        final AnnotationBo annotation = AnnotationUtils.findAnnotationBo(align.getAnnotationBoList(), AnnotationKey.API_METADATA);
        if (annotation != null) {

            final Api api = new Api();
            final ApiMetaDataBo apiMetaData = (ApiMetaDataBo) annotation.getValue();
            String apiInfo = getApiInfo(apiMetaData);
            api.setTitle(apiInfo);
            api.setDescription(apiInfo);
            if (apiMetaData.getMethodTypeEnum() == MethodTypeEnum.DEFAULT) {
                try {
                    ApiDescription apiDescription = apiDescriptionParser.parse(api.description);
                    api.setTitle(apiDescription.getSimpleMethodDescription());
                    api.setClassName(apiDescription.getSimpleClassName());
                } catch (Exception e) {
                    logger.debug("Failed to api parse. {}", api.description, e);
                }
            }
            api.setMethodTypeEnum(apiMetaData.getMethodTypeEnum());
            return api;

        } else {

            final Api api = new Api();
            AnnotationKey apiMetaDataError = getApiMetaDataError(align.getAnnotationBoList());
            api.setTitle(apiMetaDataError.getName());
            return api;
        }
    }

    private String getApiInfo(ApiMetaDataBo apiMetaDataBo) {
        if (apiMetaDataBo.getLineNumber() != -1) {
            return apiMetaDataBo.getApiInfo() + ":" + apiMetaDataBo.getLineNumber();
        } else {
            return apiMetaDataBo.getApiInfo();
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

    private static class Api {
        private String title = "";
        private String className = "";
        private String description = "";
        private MethodTypeEnum methodTypeEnum = MethodTypeEnum.DEFAULT;

        public Api() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public MethodTypeEnum getMethodTypeEnum() {
            return methodTypeEnum;
        }

        public void setMethodTypeEnum(MethodTypeEnum methodTypeEnum) {
            if (methodTypeEnum == null) {
                throw new NullPointerException("methodTypeEnum must not be null");
            }
            this.methodTypeEnum = methodTypeEnum;
        }
    }
}