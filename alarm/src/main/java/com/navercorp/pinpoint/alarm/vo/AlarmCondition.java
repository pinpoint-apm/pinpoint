package com.navercorp.pinpoint.alarm.vo;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Recursive condition tree node.
 * <p>
 * {@link Type#GROUP} node: has operator (AND/OR) and criteria (child nodes).
 * {@link Type#LEAF} node: has metric, op, threshold, etc.
 * <p>
 * Max depth is 2.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlarmCondition {

    /**
     * Node type, serialized as its name; read case-insensitively
     * ({@code "group"}/{@code "leaf"} are accepted and normalized on write).
     */
    public enum Type {
        GROUP, LEAF;

        @JsonCreator
        public static Type fromValue(String value) {
            if (GROUP.name().equalsIgnoreCase(value)) {
                return GROUP;
            }
            if (LEAF.name().equalsIgnoreCase(value)) {
                return LEAF;
            }
            throw new IllegalArgumentException("invalid condition type: " + value);
        }
    }

    public enum Operator {
        AND, OR
    }

    public enum Trigger {
        NEW_GROUP, STATUS_CHANGE, PRIORITY_CHANGE
    }

    public enum Aggregation {
        COUNT, SUM, AVG, MAX, MIN, P95, P99, RATE
    }

    public enum ThresholdType {
        ABSOLUTE, BASELINE_CHANGE
    }

    /**
     * Threshold comparison operator, serialized as its symbol ({@code ">"}, {@code ">="}, ...).
     * {@code "=="} is accepted as an alias of {@code "="} on read and normalized on write.
     */
    public enum ComparisonOp {
        GT(">"),
        GTE(">="),
        LT("<"),
        LTE("<="),
        EQ("=");

        private final String symbol;

        ComparisonOp(String symbol) {
            this.symbol = symbol;
        }

        @JsonValue
        public String symbol() {
            return symbol;
        }

        @JsonCreator
        public static ComparisonOp fromSymbol(String symbol) {
            return switch (symbol) {
                case ">" -> GT;
                case ">=" -> GTE;
                case "<" -> LT;
                case "<=" -> LTE;
                case "=", "==" -> EQ;
                default -> throw new IllegalArgumentException("invalid condition op: " + symbol);
            };
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private Type type;

    // group fields
    private Operator operator;

    @Valid
    @Size(max = AlarmValidationConstants.MAX_LEAVES_PER_GROUP + AlarmValidationConstants.MAX_GROUPS_PER_GROUP,
            message = "criteria contains too many items")
    private List<AlarmCondition> criteria;

    // leaf fields
    private Trigger trigger; // optional
    private String metric;
    private ComparisonOp op;
    private Double threshold;
    private Integer windowSec;
    private Aggregation aggregation;
    private ThresholdType thresholdType; // ABSOLUTE (default), BASELINE_CHANGE
    private Integer baselinePeriodSec;

    public AlarmCondition() {
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isGroup() {
        return type == Type.GROUP;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isLeaf() {
        return type == Type.LEAF;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public List<AlarmCondition> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<AlarmCondition> criteria) {
        this.criteria = criteria;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public ComparisonOp getOp() {
        return op;
    }

    public void setOp(ComparisonOp op) {
        this.op = op;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Integer getWindowSec() {
        return windowSec;
    }

    public void setWindowSec(Integer windowSec) {
        this.windowSec = windowSec;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public ThresholdType getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(ThresholdType thresholdType) {
        this.thresholdType = thresholdType;
    }

    public Integer getBaselinePeriodSec() {
        return baselinePeriodSec;
    }

    public void setBaselinePeriodSec(Integer baselinePeriodSec) {
        this.baselinePeriodSec = baselinePeriodSec;
    }

    @JsonAnySetter
    public void rejectUnknown(String fieldName, Object value) {
        throw new IllegalArgumentException("Unknown alarm condition field: " + fieldName);
    }
}
