/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.scatter;

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
