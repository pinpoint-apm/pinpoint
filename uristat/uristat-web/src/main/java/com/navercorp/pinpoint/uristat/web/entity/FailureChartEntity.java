/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.uristat.web.entity;

/**
 * @author intr3p1d
 */
public class FailureChartEntity extends ChartCommonEntity {
    private Double fail0;
    private Double fail1;
    private Double fail2;
    private Double fail3;
    private Double fail4;
    private Double fail5;
    private Double fail6;
    private Double fail7;

    public FailureChartEntity() {
    }

    public Double getFail0() {
        return fail0;
    }

    public void setFail0(Double fail0) {
        this.fail0 = fail0;
    }

    public Double getFail1() {
        return fail1;
    }

    public void setFail1(Double fail1) {
        this.fail1 = fail1;
    }

    public Double getFail2() {
        return fail2;
    }

    public void setFail2(Double fail2) {
        this.fail2 = fail2;
    }

    public Double getFail3() {
        return fail3;
    }

    public void setFail3(Double fail3) {
        this.fail3 = fail3;
    }

    public Double getFail4() {
        return fail4;
    }

    public void setFail4(Double fail4) {
        this.fail4 = fail4;
    }

    public Double getFail5() {
        return fail5;
    }

    public void setFail5(Double fail5) {
        this.fail5 = fail5;
    }

    public Double getFail6() {
        return fail6;
    }

    public void setFail6(Double fail6) {
        this.fail6 = fail6;
    }

    public Double getFail7() {
        return fail7;
    }

    public void setFail7(Double fail7) {
        this.fail7 = fail7;
    }
}
