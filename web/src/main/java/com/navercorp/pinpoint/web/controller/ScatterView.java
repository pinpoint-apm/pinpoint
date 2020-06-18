package com.navercorp.pinpoint.web.controller;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.Objects;

public class ScatterView {


    public static ResultView wrapResult(DotView dotView, Status status) {
        return new ResultView(dotView, status);
    }

    static class ResultView {
        @JsonUnwrapped
        private final DotView dotViewV1;
        @JsonUnwrapped
        private final Status status;

        public ResultView(DotView dotViewV1, Status status) {
            this.dotViewV1 = Objects.requireNonNull(dotViewV1, "dotResultV1");
            this.status = Objects.requireNonNull(status, "status");
        }

        public DotView getDotView() {
            return dotViewV1;
        }

        public Status getStatus() {
            return status;
        }
    }

    public static class DotView {
        private final ScatterData scatter;
        private final boolean complete;

        public DotView(ScatterData scatter, boolean complete) {
            this.scatter = Objects.requireNonNull(scatter, "scatter");
            this.complete = complete;
        }

        public ScatterData getScatter() {
            return scatter;
        }

        public long getResultFrom() {
            return scatter.getOldestAcceptedTime();
        }

        public long getResultTo() {
            return scatter.getLatestAcceptedTime();
        }

        public boolean isComplete() {
            return complete;
        }
    }

    public static class Status {
        private final long currentServerTime;
        private final long from;
        private final long to;

        public Status(long currentServerTime, Range range) {
            this(currentServerTime, range.getFrom(), range.getTo());
        }

        public Status(long currentServerTime, long from, long to) {
            this.currentServerTime = currentServerTime;
            this.from = from;
            this.to = to;
        }

        public long getCurrentServerTime() {
            return currentServerTime;
        }

        public long getFrom() {
            return from;
        }

        public long getTo() {
            return to;
        }
    }

}
