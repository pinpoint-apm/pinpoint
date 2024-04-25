package com.navercorp.pinpoint.web.scatter;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

public class ScatterView {


    public static ResultView wrapResult(DotView dotView, Status status) {
        return new ResultView(dotView, status);
    }

    public record ResultView(DotView dotView, Status status) {

        @JsonUnwrapped
        public DotView dotView() {
            return dotView;
        }

        @JsonUnwrapped
        public Status status() {
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
