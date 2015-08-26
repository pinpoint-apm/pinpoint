package com.navercorp.pinpoint.web.vo.callstacks;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.util.AnnotationUtils;
import com.navercorp.pinpoint.common.util.ApiDescription;
import com.navercorp.pinpoint.common.util.ApiDescriptionParser;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;

public class RecordFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // spans with id = 0 are regarded as root - start at 1
    private int idGen = 1;
    private final ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();

    public RecordFactory() {
    }

    public Record get(final CallTreeNode node, final String argument) {
        final SpanAlign align = node.getValue();
        align.setId(getNextId());

        final int parentId = getParentId(node);
        Api api = getApi(align);

        final Record record = new Record(align.getDepth(), align.getId(), parentId, true, api.title, argument, align.getStartTime(), align.getElapsed(), align.getGap(), align.getAgentId(), align.getApplicationId(), align.getServiceType(),
                align.getDestinationId(), align.isHasChild(), false);
        record.setSimpleClassName(api.className);
        record.setFullApiDescription(api.description);

        return record;
    }

    public Record getException(final int depth, final int parentId, final SpanAlign align) {
        if (!align.hasException()) {
            return null;
        }

        final Record record = new Record(depth, getNextId(), parentId, false, getSimpleExceptionName(align.getExceptionClass()), align.getExceptionMessage(), 0L, 0L, 0, null, null, null, null, false, true);

        return record;
    }

    private String getSimpleExceptionName(String exceptionClass) {
        if (exceptionClass == null) {
            return "";
        }
        final int index = exceptionClass.lastIndexOf('.');
        if (index != -1) {
            exceptionClass = exceptionClass.substring(index + 1, exceptionClass.length());
        }
        return exceptionClass;
    }

    public List<Record> getAnnotations(final int depth, final int parentId, SpanAlign align) {
        List<Record> list = new ArrayList<Record>();
        for (AnnotationBo annotation : align.getAnnotationBoList()) {
            final AnnotationKey key = findAnnotationKey(annotation.getKey());
            if (key.isViewInRecordSet()) {
                final Record record = new Record(depth, getNextId(), parentId, false, key.getValue(), annotation.getValue().toString(), 0L, 0L, 0, null, null, null, null, false, false);
                list.add(record);
            }
        }

        return list;
    }

    public Record getParameter(final int depth, final int parentId, final String method, final String argument) {
        return new Record(depth, getNextId(), parentId, false, method, argument, 0L, 0L, 0, null, null, null, null, false, false);
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

    Api getApi(final SpanAlign align) {
        final Api api = new Api();

        final AnnotationBo annotation = AnnotationUtils.findAnnotationBo(align.getAnnotationBoList(), AnnotationKey.API_METADATA);

        if (annotation != null) {
            final ApiMetaDataBo apiMetaData = (ApiMetaDataBo) annotation.getValue();
            api.title = api.description = getApiInfo(apiMetaData);
            try {
                ApiDescription apiDescription = apiDescriptionParser.parse(api.description);
                api.title = apiDescription.getSimpleMethodDescription();
                api.className = apiDescription.getSimpleClassName();
            } catch(Exception ignored) {
                logger.error("Invalid api description {}", api.description, ignored);
            }
        } else {
            AnnotationKey apiMetaDataError =  AnnotationUtils.getApiMetaDataError(align.getAnnotationBoList());
            api.title = apiMetaDataError.getValue();
        }

        return api;
    }

    private String getApiInfo(ApiMetaDataBo apiMetaDataBo) {
        if (apiMetaDataBo.getLineNumber() != -1) {
            return apiMetaDataBo.getApiInfo() + ":" + apiMetaDataBo.getLineNumber();
        } else {
            return apiMetaDataBo.getApiInfo();
        }
    }

    private AnnotationKey findAnnotationKey(int key) {
        return AnnotationKey.findAnnotationKey(key);
    }

    private int getNextId() {
        return idGen++;
    }

    private class Api {
        private String title = "";
        private String className = "";
        private String description = "";
        private int type = 0;
    }
}