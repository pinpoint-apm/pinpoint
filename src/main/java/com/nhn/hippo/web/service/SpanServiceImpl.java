package com.nhn.hippo.web.service;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.calltree.span.SpanAligner;
import com.nhn.hippo.web.calltree.span.SpanPopulator;
import com.nhn.hippo.web.dao.SqlMetaDataDao;
import com.nhn.hippo.web.dao.TraceDao;
import com.profiler.common.AnnotationNames;
import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SqlMetaDataBo;
import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.common.mapping.ApiUtils;
import com.profiler.common.mapping.MethodMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@Service
public class SpanServiceImpl implements SpanService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private SqlMetaDataDao sqlMetaDataDao;

    @Override
    public List<SpanAlign> selectSpan(String uuid) {
        UUID id = UUID.fromString(uuid);
        List<SpanBo> spans = traceDao.selectSpanAndAnnotation(id);
        if (spans == null || spans.isEmpty()) {
            return Collections.emptyList();
        }

        List<SpanAlign> order = order(spans);
        transitionApiId(order);
        transitionSqlId(order);
        // TODO root span not found시 row data라도 보여줘야 됨.
        if (order.size() != spans.size()) {
            // TODO 중간 노드 데이터 분실 ? 혹은 잘못된 데이터 생성?
            logger.info("span node not complete! ");
        }
        return order;

    }


    private void transitionAnnotation(List<SpanAlign> spans, AnnotationReplacementCallback annotationReplacementCallback) {
        for (SpanAlign spanAlign : spans) {
            List<AnnotationBo> annotationBoList;
            if (spanAlign.isRoot()) {
                annotationBoList = spanAlign.getSpan().getAnnotationBoList();
                annotationReplacementCallback.replacement(spanAlign, annotationBoList);
            } else {
                annotationBoList = spanAlign.getSubSpanBo().getAnnotationBoList();
                annotationReplacementCallback.replacement(spanAlign, annotationBoList);
            }
        }
    }

    private void transitionSqlId(final List<SpanAlign> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {
                for (AnnotationBo annotationBo : annotationBoList) {
                    // TODO SQL-ID 일단 날코딩 나중에 뭔가 key를 따자
                    if ("SQL-ID".equals(annotationBo.getKey())) {

                        String agentId = getAgentId(spanAlign);
                        // TODO 일단 시간까지 조회는 하지 말고 하자.
                        int hashCode = (Integer) annotationBo.getValue();
                        List<SqlMetaDataBo> sqlMetaDataList = sqlMetaDataDao.getSqlMetaData(agentId, hashCode, 0);
                        int size = sqlMetaDataList.size();
                        if (size == 0) {
                            AnnotationBo api = new AnnotationBo();
                            api.setKey(AnnotationNames.SQL_METADATA);
                            api.setValue("SQL-ID not found hashCode:" + hashCode);
                            annotationBoList.add(api);
                        } else if (size == 1) {
                            AnnotationBo api = new AnnotationBo();
                            api.setKey(AnnotationNames.SQL_METADATA);
                            api.setValue(sqlMetaDataList.get(0).getSql());
                            annotationBoList.add(api);
                        } else {
                            AnnotationBo api = new AnnotationBo();
                            api.setKey(AnnotationNames.SQL_METADATA);
                            api.setValue(collisionSqlHashCodeMessage(hashCode, sqlMetaDataList));
                            annotationBoList.add(api);
                        }

                        break;
                    }
                }
            }
        });
    }

    private String collisionSqlHashCodeMessage(int hashCode, List<SqlMetaDataBo> sqlMetaDataList) {
        // TODO 이거 체크하는 테스트를 따로 만들어야 될듯 하다. 왠간하면 확율상 hashCode 충돌 케이스를 쉽게 만들수 없음.
        StringBuilder sb = new StringBuilder(64);
        sb.append("Collision Sql hashCode:");
        sb.append(hashCode);
        sb.append('\n');
        for (int i = 0; i < sqlMetaDataList.size(); i++) {
            if (i != 0) {
                sb.append("or\n");
            }
            SqlMetaDataBo sqlMetaDataBo = sqlMetaDataList.get(i);
            sb.append(sqlMetaDataBo.getSql());
        }
        return sb.toString();
    }

    private String getAgentId(SpanAlign spanAlign) {
        if (spanAlign.isRoot()) {
            return spanAlign.getSpan().getAgentId();
        } else {
            return spanAlign.getSubSpanBo().getAgentId();
        }
    }

    private void transitionApiId(List<SpanAlign> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {
                for (AnnotationBo annotationBo : annotationBoList) {
                    // TODO API-ID 일단 날코딩 나중에 뭔가 key를 따자
                    if ("API-ID".equals(annotationBo.getKey())) {
                        MethodMapping methodMapping = ApiMappingTable.findMethodMapping((Integer) annotationBo.getValue());
                        if (methodMapping == null) {
                            continue;
                        }
                        String className = methodMapping.getClassMapping().getClassName();
                        String methodName = methodMapping.getMethodName();
                        String[] parameterType = methodMapping.getParameterType();
                        String[] parameterName = methodMapping.getParameterName();
                        String args = ApiUtils.mergeParameterVariableNameDescription(parameterType, parameterName);
                        AnnotationBo api = new AnnotationBo();
                        api.setKey("API");
                        api.setValue(className + "." + methodName + args);
                        annotationBoList.add(api);
                        break;
                    }
                }
            }
        });
    }

    public static interface AnnotationReplacementCallback {
        void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList);
    }

    private List<SpanAlign> order(List<SpanBo> spans) {
        SpanAligner spanAligner = new SpanAligner(spans);
        List<SpanAlign> sort = spanAligner.sort();
        SpanPopulator spanPopulator = new SpanPopulator(sort);
        List<SpanAlign> populatedList = spanPopulator.populateSubSpan();
        return populatedList;
    }
}
