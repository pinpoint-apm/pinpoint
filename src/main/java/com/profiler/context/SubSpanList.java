package com.profiler.context;

import com.profiler.Agent;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class SubSpanList implements Thriftable {

    private List<SubSpan> subSpanList = new ArrayList<SubSpan>();

    public SubSpanList(List<SubSpan> subSpanList) {
        this.subSpanList = subSpanList;
    }

    @Override
    public TBase toThrift() {
        com.profiler.common.dto.thrift.SubSpanList tSubSpanList = new com.profiler.common.dto.thrift.SubSpanList();
        // TODO 반드시 1개 이상이라는 조건을 충족해야 된다.
        SubSpan first = subSpanList.get(0);
        Span parentSpan = first.getParentSpan();
        tSubSpanList.setAgentId(Agent.getInstance().getAgentId());
        UUID id = parentSpan.getTraceID().getId();
        tSubSpanList.setMostTraceId(id.getMostSignificantBits());
        tSubSpanList.setLeastTraceId(id.getLeastSignificantBits());
        tSubSpanList.setSpanId(parentSpan.getTraceID().getSpanId());

        List<com.profiler.common.dto.thrift.SubSpan> tSubSpan = createSubSpan(subSpanList);

        tSubSpanList.setSubSpanList(tSubSpan);

        return tSubSpanList;
    }

    private List<com.profiler.common.dto.thrift.SubSpan> createSubSpan(List<SubSpan> subSpanList) {
        List<com.profiler.common.dto.thrift.SubSpan> result = new ArrayList<com.profiler.common.dto.thrift.SubSpan>(subSpanList.size());
        for (SubSpan subSpan : subSpanList) {
            com.profiler.common.dto.thrift.SubSpan tSubSpan = new com.profiler.common.dto.thrift.SubSpan();

            tSubSpan.setAgentId(Agent.getInstance().getAgentId());

            long parentSpanStartTime = subSpan.getStartTime();
            tSubSpan.setStartElapsed((int) (subSpan.getStartTime() - parentSpanStartTime));
            tSubSpan.setEndElapsed((int) (subSpan.getEndTime() - subSpan.getStartTime()));

            tSubSpan.setSequence(subSpan.getSequence());

            tSubSpan.setRpc(subSpan.getRpc());
            tSubSpan.setServiceName(subSpan.getServiceName());
            tSubSpan.setServiceType(subSpan.getServiceType().getCode());

            tSubSpan.setEndPoint(subSpan.getEndPoint());

            // 여기서 데이터 인코딩을 하자.
            List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(subSpan.getAnnotationSize());
            for (HippoAnnotation a : subSpan.getAnnotations()) {
                annotationList.add(a.toThrift());
            }
            tSubSpan.setAnnotations(annotationList);
            result.add(tSubSpan);
        }
        return result;
    }
}
