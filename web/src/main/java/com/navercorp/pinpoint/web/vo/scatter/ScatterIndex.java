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

    private static final int x = 0;
    private static final int y = 1;
    private static final int transactionId = 2;
    private static final int type = 3;

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
