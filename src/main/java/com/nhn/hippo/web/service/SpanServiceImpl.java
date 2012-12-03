package com.nhn.hippo.web.service;

import java.util.*;

import com.profiler.common.bo.SubSpanBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.calltree.span.SpanAligner;
import com.nhn.hippo.web.dao.TraceDao;
import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.common.mapping.ApiUtils;
import com.profiler.common.mapping.MethodMapping;

/**
 *
 */
@Service
public class SpanServiceImpl implements SpanService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TraceDao traceDao;

    @Override
    public List<SpanAlign> selectSpan(String uuid) {
        UUID id = UUID.fromString(uuid);
        List<SpanBo> spans = traceDao.selectSpanAndAnnotation(id);
        if (spans == null) {
            return Collections.emptyList();
        }

        List<SpanAlign> order = order(spans);
        List<SpanAlign> populatedList = populateSubSpan(order);
        transitionApiId(populatedList);
        // TODO root span not found시 row data라도 보여줘야 됨.
        if (order.size() != spans.size()) {
            // TODO 중간 노드 데이터 분실 ? 혹은 잘못된 데이터 생성?
            logger.info("span node not complete! ");
        }
        return populatedList;

    }

    private List populateSubSpan(List<SpanAlign> order) {
        List<SpanAlign> populatedList = new ArrayList<SpanAlign>();

        for (int i = 0; i < order.size(); i++) {
            populatedSpan(populatedList, order, i);
        }
        return populatedList;
    }

    private void populatedSpan(List<SpanAlign> populatedList, List<SpanAlign> order, int i) {
        SpanAlign spanAlign = order.get(i);
        SpanBo span = spanAlign.getSpan();
        populatedList.add(spanAlign);
        long startTime = span.getStartTime();
        List<SubSpanBo> subSpanBos = sortSubSpan(span);
        for (SubSpanBo subSpanBo : subSpanBos) {
            long subStartTime = startTime + subSpanBo.getStartElapsed();
            long nextSpanStartTime = getNextSpanStartTime(order, i);
            if (subStartTime >= nextSpanStartTime) {
                SpanAlign subSpanAlign = new SpanAlign(spanAlign.getDepth(), span, subSpanBo);
                subSpanAlign.setRoot(false);
                populatedList.add(subSpanAlign);
            } else {
                populatedSpan(populatedList, order, i);
            }
        }
    }

    public long getNextSpanStartTime(List<SpanAlign> order, int i) {
        if (i < order.size()) {
            return 0;
        }
        return order.get(i + 1).getSpan().getStartTime();
    }

    private List<SubSpanBo> sortSubSpan(SpanBo span) {
        List<SubSpanBo> subSpanList = span.getSubSpanList();
        if (subSpanList == null) {
            return Collections.emptyList();
        }
        Collections.sort(subSpanList, new Comparator<SubSpanBo>() {
            @Override
            public int compare(SubSpanBo o1, SubSpanBo o2) {
                long o1Timestamp = o1.getSequence();
                long o2Timestamp = o2.getSequence();
                if (o1Timestamp > o2Timestamp) {
                    return 1;
                }
                if (o1Timestamp == o2Timestamp) {
                    return 0;
                }
                return -1;
            }
        });
        return subSpanList;
    }

    private void transitionApiId(List<SpanAlign> spans) {
        for (SpanAlign spanBo : spans) {
            List<AnnotationBo> annotationBoList;
            if (spanBo.isRoot()) {
                annotationBoList = spanBo.getSpan().getAnnotationBoList();
            } else {
                annotationBoList = spanBo.getSubSpanBo().getAnnotationBoList();
            }
            for (AnnotationBo annotationBo : annotationBoList) {
                // TODO API-ID 일단 날코딩 나중에 뭔가 key를 따자
                if ("API-ID".equals(annotationBo.getKey())) {
                    MethodMapping methodMapping = null;
                    try {
                        methodMapping = ApiMappingTable.findMethodMapping((Integer) annotationBo.getValue());
                    } catch (Exception e) {
                        e.printStackTrace(); // To change body of catch
                        // statement use File | Settings
                        // | File Templates.
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
    }

    private List<SpanAlign> order(List<SpanBo> spans) {

        SpanAligner spanAligner = new SpanAligner(spans);
        return spanAligner.sort();
    }
}
