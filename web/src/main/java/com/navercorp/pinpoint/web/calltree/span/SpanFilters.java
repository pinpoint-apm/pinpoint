package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.controller.BusinessTransactionController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class SpanFilters {
    private static long DEFAULT_FOCUS_TIMESTAMP = Long.parseLong(BusinessTransactionController.DEFAULT_FOCUS_TIMESTAMP);
    private static long DEFAULT_SPANID = Long.parseLong(BusinessTransactionController.DEFAULT_SPANID);


    public static Predicate<SpanBo> spanFilter(long spanId, String agentId, long focusTimestamp) {
        FilterBuilder builder = newBuilder();

        if (spanId != DEFAULT_SPANID) {
            Predicate<SpanBo> filter = SpanFilters.spanIdFilter(spanId);
            builder.addFilter(filter);
        }
        if (StringUtils.hasLength(agentId)) {
            Predicate<SpanBo> filter = SpanFilters.agentIdFilter(agentId);
            builder.addFilter(filter);
        }
        if (focusTimestamp != DEFAULT_FOCUS_TIMESTAMP) {
            Predicate<SpanBo> filter = SpanFilters.collectorAcceptTimeFilter(focusTimestamp);
            builder.addFilter(filter);
        }
//
//        Predicate<SpanBo> filter = filterChain.stream()
//                .reduce(x -> true, Predicate::and);
//        return filter;
        return builder.build();
    }

    public static FilterBuilder newBuilder() {
        return new FilterBuilder();
    }

    public static class FilterBuilder {
        private final List<Predicate<SpanBo>> predicates = new ArrayList<>();

        public void addFilter(Predicate<SpanBo> filter) {
            Objects.requireNonNull(filter, "filter");
            this.predicates.add(filter);
        }

        public Predicate<SpanBo> build() {
            return new PredicateChain(predicates);
        }
    }

    public static class PredicateChain implements Predicate<SpanBo> {
        private final Predicate<SpanBo>[] predicates;

        public PredicateChain(List<Predicate<SpanBo>> predicates) {
            Objects.requireNonNull(predicates, "predicates");
            this.predicates = predicates.toArray(new Predicate[0]);
        }

        @Override
        public boolean test(SpanBo spanBo) {
            for (Predicate<SpanBo> predicate : predicates) {
                if (!predicate.test(spanBo)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "PredicateChain{" +
                    "predicates=" + Arrays.toString(predicates) +
                    '}';
        }
    }

    public static Predicate<SpanBo> collectorAcceptTimeFilter(final long collectorAcceptTime) {
        return new CollectorAcceptTimePredicate(collectorAcceptTime);
    }


    private static class CollectorAcceptTimePredicate implements Predicate<SpanBo> {
        private final long collectorAcceptTime;

        public CollectorAcceptTimePredicate(long collectorAcceptTime) {
            this.collectorAcceptTime = collectorAcceptTime;
        }

        @Override
        public boolean test(SpanBo spanBo) {
            return spanBo.getCollectorAcceptTime() == collectorAcceptTime;
        }

        @Override
        public String toString() {
            return "collectorAcceptTimeFilter:" + collectorAcceptTime;
        }
    }

    public static Predicate<SpanBo> spanIdFilter(final long spanId) {
        return new SpanIdPredicate(spanId);
    }


    private static class SpanIdPredicate implements Predicate<SpanBo> {
        private final long spanId;

        public SpanIdPredicate(long spanId) {
            this.spanId = spanId;
        }

        @Override
        public boolean test(SpanBo spanBo) {
            return spanBo.getSpanId() == spanId;
        }

        @Override
        public String toString() {
            return "spanIdFilter:" + spanId;
        }
    }

    public static Predicate<SpanBo> agentIdFilter(String agentId) {
        return new SpanAgentIdPredicate(agentId);
    }

    private static class SpanAgentIdPredicate implements Predicate<SpanBo> {
        private final String agentId;

        public SpanAgentIdPredicate(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
        }

        @Override
        public boolean test(SpanBo spanBo) {
            return agentId.equals(spanBo.getAgentId());
        }

        @Override
        public String toString() {
            return "agentIdFilter:" + agentId;
        }
    }

    // -----------------------
    // SpanQueryBuilder
    public static Predicate<SpanBo> transactionIdFilter(TransactionId transactionId) {
        Objects.requireNonNull(transactionId, "transactionId");
        return new Predicate<SpanBo>() {
            @Override
            public boolean test(SpanBo spanBo) {
                return transactionId.equals(spanBo.getTransactionId());
            }

            @Override
            public String toString() {
                return "transactionId=" + transactionId;
            }
        };
    }

    public static Predicate<SpanBo> applicationIdFilter(String applicationId) {
        Objects.requireNonNull(applicationId, "applicationId");
        return new Predicate<SpanBo>() {
            @Override
            public boolean test(SpanBo spanBo) {
                return applicationId.equals(spanBo.getApplicationId());
            }

            @Override
            public String toString() {
                return "applicationId=" + applicationId;
            }
        };
    }


    public static Predicate<SpanBo> responseTimeFilter(int responseTime) {
        return new Predicate<SpanBo>() {
            @Override
            public boolean test(SpanBo spanBo) {
                return responseTime == spanBo.getElapsed();
            }

            @Override
            public String toString() {
                return "responseTime=" + responseTime;
            }
        };
    }





}
