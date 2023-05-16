package com.navercorp.pinpoint.web.scatter;

import com.fasterxml.jackson.annotation.JsonUnwrapped;


import java.util.Objects;

public class ScatterView {


    public static ResultView wrapResult(DotView dotView, Status status) {
        return new ResultView(dotView, status);
    }

    public static class ResultView {

        private final DotView dotViewV1;

        private final Status status;

        public ResultView(DotView dotViewV1, Status status) {
            this.dotViewV1 = Objects.requireNonNull(dotViewV1, "dotResultV1");
            this.status = Objects.requireNonNull(status, "status");
        }

        @JsonUnwrapped
        public DotView getDotView() {
            return dotViewV1;
        }

        @JsonUnwrapped
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

}
