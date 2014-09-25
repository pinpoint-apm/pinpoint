package com.nhn.pinpoint.web.vo.scatter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author emeroad
 */
public class ScatterIndex {
    public static final ScatterIndex MATA_DATA = new ScatterIndex();
//    "scatterIndex" : {
//          "x":0,
//          "y":1,
//          "transactionId":2,
//          "type":3
//     },

    private final int x = 0;
    private final int y = 1;
    private final int transactionId = 2;
    private final int type = 3;

    @JsonProperty("x")
    public int getX() {
        return x;
    }

    @JsonProperty("y")
    public int getY() {
        return y;
    }

    @JsonProperty("transactionId")
    public int getTransactionId() {
        return transactionId;
    }

    @JsonProperty("type")
    public int getType() {
        return type;
    }
}
