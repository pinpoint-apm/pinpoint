package com.nhn.pinpoint.web.calltree.span;

import com.nhn.pinpoint.common.bo.SpanBo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class SpanIdMatcher {
    private List<SpanBo> nextSpanBoList;

    private final long MAX_EXCLUDE_WEIGHT = 1000 * 5;

    public SpanIdMatcher(List<SpanBo> nextSpanBoList) {
        if (nextSpanBoList == null) {
            throw new NullPointerException("nextSpanBoList must not be null");
        }
        this.nextSpanBoList = nextSpanBoList;
    }

    public SpanBo approximateMatch(long spanEventBoStartTime) {
        // 매칭 알고리즘이 있어야 함.
        List<WeightSpanBo> weightSpanList = computeWeight(spanEventBoStartTime);
        if (weightSpanList.size() == 0) {
            return null;
        }
        Collections.sort(weightSpanList, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                WeightSpanBo wSpan1 = (WeightSpanBo) o1;
                WeightSpanBo wSpan2 = (WeightSpanBo) o2;
                final long spanWeight1 = wSpan1.getWeight();
                final long spanWeight2 = wSpan2.getWeight();
                if (spanWeight1 < spanWeight2) {
                    return -1;
                } else {
                    if (spanWeight1 == spanWeight2) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            }
        });

        SpanBo minWeight = getMinWeight(weightSpanList);
        if (minWeight != null) {
            nextSpanBoList.remove(minWeight);
        }
        return minWeight;
    }

    private SpanBo getMinWeight(List<WeightSpanBo> weightSpanList) {
        long min = Long.MAX_VALUE;
        final List<SpanBo> minValue = new ArrayList<SpanBo>();
        for (WeightSpanBo weightSpanBo : weightSpanList) {
            long weight = weightSpanBo.getWeight();
            if (weight <= min) {
                minValue.add(weightSpanBo.getSpanBo());
                min = weight;
            }
        }

        if (minValue.size() == 1) {
            return minValue.get(0);
        }
        // 2개 이상일 경우일단 그냥 앞선 데이터를 던짐.
        // 뭔가 로그 필요.
        return minValue.get(0);
    }

    private List<WeightSpanBo> computeWeight(long spanEventBoStartTime) {
        List<WeightSpanBo> weightSpanList = new ArrayList<WeightSpanBo>();
        for (SpanBo next : nextSpanBoList) {
            long startTime = next.getStartTime();
            long distance = startTime - spanEventBoStartTime;
            long weightDistance = getWeightDistance(distance);
            if (weightDistance > MAX_EXCLUDE_WEIGHT) {
                // MAX WEIGHT보다 가중치가 높을 경우. 분실된 케이스 일수 있으므로 그냥 버린다.
                continue;
            }
            weightSpanList.add(new WeightSpanBo(weightDistance, next));
        }
        return weightSpanList;
    }

    private long getWeightDistance(long distance) {
        if (distance >= 0) {
            // 양수일 경우
            return distance;
        } else {
            // 음수일 경우 패널티를 둔다. 네트워크 타임 동기화 시간이 길지 않을 경우 음수가  매치될 확율은 매우 적어야 한다.
            // 차라리 jvm gc등으로 인해 양수 값이 많이 차이 날수 있지. 음수값이 매치될 가능성은 매우낮다고 봐야 한다.
            // 네트워크 싱크 시간?? 오차 등을 추가로 더하면 될거 같은데. 모르니깐 대충 더하자. 패널티는 1초
            distance = Math.abs(distance);
            return (distance * 2) + 1000;
        }
    }


    public List<SpanBo> other() {
        if (nextSpanBoList.size() == 0) {
            return null;
        }
        return nextSpanBoList;
    }

    private static class WeightSpanBo {
        private long weight;
        private SpanBo spanBo;

        private WeightSpanBo(long weight, SpanBo spanBo) {
            this.weight = weight;
            this.spanBo = spanBo;
        }

        private long getWeight() {
            return weight;
        }

        private SpanBo getSpanBo() {
            return spanBo;
        }
    }
}
