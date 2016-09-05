/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.chart;

/**
 * @author hyungil.jeong
 */
@Deprecated
public class DataPoint<X extends Number, Y extends Number> {

    private final X xVal;
    private final Y yVal;

    public DataPoint(X xVal, Y yVal) {
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public X getXVal() {
        return xVal;
    }

    public Y getYVal() {
        return yVal;
    }

    @Override
    public String toString() {
        return "(" + xVal + "," + yVal + ")";
    }
}
