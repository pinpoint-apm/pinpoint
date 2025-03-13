/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.vo;

import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class HeatmapStat {

    private final String applicationName;
    private final String agentId;
    private final long eventTime;

    private int suc002 = 0; // 0ms~200ms

    private int suc004 = 0; // 200ms~400ms
    private int suc006 = 0; // 400ms~600ms
    private int suc008 = 0; // 600ms~800ms
    private int suc010 = 0; // 800ms~1000ms
    private int suc012 = 0; // 1000ms~1200ms
    private int suc014 = 0; // 1200ms~1400ms
    private int suc016 = 0; // 1400ms~1600ms
    private int suc018 = 0; // 1600ms~1800ms
    private int suc020 = 0; // 1800ms~2000ms
    private int suc022 = 0; // 2000ms~2200ms
    private int suc024 = 0; // 2200ms~2400ms
    private int suc026 = 0; // 2400ms~2600ms
    private int suc028 = 0; // 2600ms~2800ms
    private int suc030 = 0; // 2800ms~3000ms
    private int suc032 = 0; // 3000ms~3200ms
    private int suc034 = 0; // 3200ms~3400ms
    private int suc036 = 0; // 3400ms~3600ms
    private int suc038 = 0; // 3600ms~3800ms
    private int suc040 = 0; // 3800ms~4000ms
    private int suc042 = 0; // 4000ms~4200ms
    private int suc044 = 0; // 4200ms~4400ms
    private int suc046 = 0; // 4400ms~4600ms
    private int suc048 = 0; // 4600ms~4800ms
    private int suc050 = 0; // 4800ms~5000ms
    private int suc052 = 0; // 5000ms~5200ms
    private int suc054 = 0; // 5200ms~5400ms
    private int suc056 = 0; // 5400ms~5600ms
    private int suc058 = 0; // 5600ms~5800ms
    private int suc060 = 0; // 5800ms~6000ms
    private int suc062 = 0; // 6000ms~6200ms
    private int suc064 = 0; // 6200ms~6400ms
    private int suc066 = 0; // 6400ms~6600ms
    private int suc068 = 0; // 6600ms~6800ms
    private int suc070 = 0; // 6800ms~7000ms
    private int suc072 = 0; // 7000ms~7200ms
    private int suc074 = 0; // 7200ms~7400ms
    private int suc076 = 0; // 7400ms~7600ms
    private int suc078 = 0; // 7600ms~7800ms
    private int suc080 = 0; // 7800ms~8000ms
    private int suc082 = 0; // 8000ms~8200ms
    private int suc084 = 0; // 8200ms~8400ms
    private int suc086 = 0; // 8400ms~8600ms
    private int suc088 = 0; // 8600ms~8800ms
    private int suc090 = 0; // 8800ms~9000ms
    private int suc092 = 0; // 9000ms~9200ms
    private int suc094 = 0; // 9200ms~9400ms
    private int suc096 = 0; // 9400ms~9600ms
    private int suc098 = 0; // 9600ms~9800ms
    private int suc100 = 0; // 9800ms~

    private int fal002 = 0; // 0ms~200ms
    private int fal004 = 0; // 200ms~400ms
    private int fal006 = 0; // 400ms~600ms
    private int fal008 = 0; // 600ms~800ms
    private int fal010 = 0; // 800ms~1000ms
    private int fal012 = 0; // 1000ms~1200ms
    private int fal014 = 0; // 1200ms~1400ms
    private int fal016 = 0; // 1400ms~1600ms
    private int fal018 = 0; // 1600ms~1800ms
    private int fal020 = 0; // 1800ms~2000ms
    private int fal022 = 0; // 2000ms~2200ms
    private int fal024 = 0; // 2200ms~2400ms
    private int fal026 = 0; // 2400ms~2600ms
    private int fal028 = 0; // 2600ms~2800ms
    private int fal030 = 0; // 2800ms~3000ms
    private int fal032 = 0; // 3000ms~3200ms
    private int fal034 = 0; // 3200ms~3400ms
    private int fal036 = 0; // 3400ms~3600ms
    private int fal038 = 0; // 3600ms~3800ms
    private int fal040 = 0; // 3800ms~4000ms
    private int fal042 = 0; // 4000ms~4200ms
    private int fal044 = 0; // 4200ms~4400ms
    private int fal046 = 0; // 4400ms~4600ms
    private int fal048 = 0; // 4600ms~4800ms
    private int fal050 = 0; // 4800ms~5000ms
    private int fal052 = 0; // 5000ms~5200ms
    private int fal054 = 0; // 5200ms~5400ms
    private int fal056 = 0; // 5400ms~5600ms
    private int fal058 = 0; // 5600ms~5800ms
    private int fal060 = 0; // 5800ms~6000ms
    private int fal062 = 0; // 6000ms~6200ms
    private int fal064 = 0; // 6200ms~6400ms
    private int fal066 = 0; // 6400ms~6600ms
    private int fal068 = 0; // 6600ms~6800ms
    private int fal070 = 0; // 6800ms~7000ms
    private int fal072 = 0; // 7000ms~7200ms
    private int fal074 = 0; // 7200ms~7400ms
    private int fal076 = 0; // 7400ms~7600ms
    private int fal078 = 0; // 7600ms~7800ms
    private int fal080 = 0; // 7800ms~8000ms
    private int fal082 = 0; // 8000ms~8200ms
    private int fal084 = 0; // 8200ms~8400ms
    private int fal086 = 0; // 8400ms~8600ms
    private int fal088 = 0; // 8600ms~8800ms
    private int fal090 = 0; // 8800ms~9000ms
    private int fal092 = 0; // 9000ms~9200ms
    private int fal094 = 0; // 9200ms~9400ms
    private int fal096 = 0; // 9400ms~9600ms
    private int fal098 = 0; // 9600ms~9800ms
    private int fal100 = 0; // 9800ms~

    public HeatmapStat(SpanStat spanStat) {
        this.applicationName = Objects.requireNonNull(spanStat.getApplicationName(), "applicationName");
        this.agentId = Objects.requireNonNull(spanStat.getAgentId(), "agentId");
        this.eventTime = spanStat.getStartTime();
        int elapsed = spanStat.getElapsed();

        if (spanStat.isSuccess()) {
            if (elapsed <= 200) suc002 = 1;
            else if (elapsed <= 400) suc004 = 1;
            else if (elapsed <= 600) suc006 = 1;
            else if (elapsed <= 800) suc008 = 1;
            else if (elapsed <= 1000) suc010 = 1;
            else if (elapsed <= 1200) suc012 = 1;
            else if (elapsed <= 1400) suc014 = 1;
            else if (elapsed <= 1600) suc016 = 1;
            else if (elapsed <= 1800) suc018 = 1;
            else if (elapsed <= 2000) suc020 = 1;
            else if (elapsed <= 2200) suc022 = 1;
            else if (elapsed <= 2400) suc024 = 1;
            else if (elapsed <= 2600) suc026 = 1;
            else if (elapsed <= 2800) suc028 = 1;
            else if (elapsed <= 3000) suc030 = 1;
            else if (elapsed <= 3200) suc032 = 1;
            else if (elapsed <= 3400) suc034 = 1;
            else if (elapsed <= 3600) suc036 = 1;
            else if (elapsed <= 3800) suc038 = 1;
            else if (elapsed <= 4000) suc040 = 1;
            else if (elapsed <= 4200) suc042 = 1;
            else if (elapsed <= 4400) suc044 = 1;
            else if (elapsed <= 4600) suc046 = 1;
            else if (elapsed <= 4800) suc048 = 1;
            else if (elapsed <= 5000) suc050 = 1;
            else if (elapsed <= 5200) suc052 = 1;
            else if (elapsed <= 5400) suc054 = 1;
            else if (elapsed <= 5600) suc056 = 1;
            else if (elapsed <= 5800) suc058 = 1;
            else if (elapsed <= 6000) suc060 = 1;
            else if (elapsed <= 6200) suc062 = 1;
            else if (elapsed <= 6400) suc064 = 1;
            else if (elapsed <= 6600) suc066 = 1;
            else if (elapsed <= 6800) suc068 = 1;
            else if (elapsed <= 7000) suc070 = 1;
            else if (elapsed <= 7200) suc072 = 1;
            else if (elapsed <= 7400) suc074 = 1;
            else if (elapsed <= 7600) suc076 = 1;
            else if (elapsed <= 7800) suc078 = 1;
            else if (elapsed <= 8000) suc080 = 1;
            else if (elapsed <= 8200) suc082 = 1;
            else if (elapsed <= 8400) suc084 = 1;
            else if (elapsed <= 8600) suc086 = 1;
            else if (elapsed <= 8800) suc088 = 1;
            else if (elapsed <= 9000) suc090 = 1;
            else if (elapsed <= 9200) suc092 = 1;
            else if (elapsed <= 9400) suc094 = 1;
            else if (elapsed <= 9600) suc096 = 1;
            else if (elapsed <= 9800) suc098 = 1;
            else suc100 = 1;
        } else {
            if (elapsed <= 200) fal002 = 1;
            else if (elapsed <= 400) fal004 = 1;
            else if (elapsed <= 600) fal006 = 1;
            else if (elapsed <= 800) fal008 = 1;
            else if (elapsed <= 1000) fal010 = 1;
            else if (elapsed <= 1200) fal012 = 1;
            else if (elapsed <= 1400) fal014 = 1;
            else if (elapsed <= 1600) fal016 = 1;
            else if (elapsed <= 1800) fal018 = 1;
            else if (elapsed <= 2000) fal020 = 1;
            else if (elapsed <= 2200) fal022 = 1;
            else if (elapsed <= 2400) fal024 = 1;
            else if (elapsed <= 2600) fal026 = 1;
            else if (elapsed <= 2800) fal028 = 1;
            else if (elapsed <= 3000) fal030 = 1;
            else if (elapsed <= 3200) fal032 = 1;
            else if (elapsed <= 3400) fal034 = 1;
            else if (elapsed <= 3600) fal036 = 1;
            else if (elapsed <= 3800) fal038 = 1;
            else if (elapsed <= 4000) fal040 = 1;
            else if (elapsed <= 4200) fal042 = 1;
            else if (elapsed <= 4400) fal044 = 1;
            else if (elapsed <= 4600) fal046 = 1;
            else if (elapsed <= 4800) fal048 = 1;
            else if (elapsed <= 5000) fal050 = 1;
            else if (elapsed <= 5200) fal052 = 1;
            else if (elapsed <= 5400) fal054 = 1;
            else if (elapsed <= 5600) fal056 = 1;
            else if (elapsed <= 5800) fal058 = 1;
            else if (elapsed <= 6000) fal060 = 1;
            else if (elapsed <= 6200) fal062 = 1;
            else if (elapsed <= 6400) fal064 = 1;
            else if (elapsed <= 6600) fal066 = 1;
            else if (elapsed <= 6800) fal068 = 1;
            else if (elapsed <= 7000) fal070 = 1;
            else if (elapsed <= 7200) fal072 = 1;
            else if (elapsed <= 7400) fal074 = 1;
            else if (elapsed <= 7600) fal076 = 1;
            else if (elapsed <= 7800) fal078 = 1;
            else if (elapsed <= 8000) fal080 = 1;
            else if (elapsed <= 8200) fal082 = 1;
            else if (elapsed <= 8400) fal084 = 1;
            else if (elapsed <= 8600) fal086 = 1;
            else if (elapsed <= 8800) fal088 = 1;
            else if (elapsed <= 9000) fal090 = 1;
            else if (elapsed <= 9200) fal092 = 1;
            else if (elapsed <= 9400) fal094 = 1;
            else if (elapsed <= 9600) fal096 = 1;
            else if (elapsed <= 9800) fal098 = 1;
            else fal100 = 1;
        }
    }

    public String getAgentId() {
        return agentId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public long getEventTime() {
        return eventTime;
    }


    public int getSuc002() {
        return suc002;
    }

    public int getSuc004() {
        return suc004;
    }

    public int getSuc006() {
        return suc006;
    }

    public int getSuc008() {
        return suc008;
    }

    public int getSuc010() {
        return suc010;
    }

    public int getSuc012() {
        return suc012;
    }

    public int getSuc014() {
        return suc014;
    }

    public int getSuc016() {
        return suc016;
    }

    public int getSuc018() {
        return suc018;
    }

    public int getSuc020() {
        return suc020;
    }

    public int getSuc022() {
        return suc022;
    }

    public int getSuc024() {
        return suc024;
    }

    public int getSuc026() {
        return suc026;
    }

    public int getSuc028() {
        return suc028;
    }

    public int getSuc030() {
        return suc030;
    }

    public int getSuc032() {
        return suc032;
    }

    public int getSuc034() {
        return suc034;
    }

    public int getSuc036() {
        return suc036;
    }

    public int getSuc038() {
        return suc038;
    }

    public int getSuc040() {
        return suc040;
    }

    public int getSuc042() {
        return suc042;
    }

    public int getSuc044() {
        return suc044;
    }

    public int getSuc046() {
        return suc046;
    }

    public int getSuc048() {
        return suc048;
    }

    public int getSuc050() {
        return suc050;
    }

    public int getSuc052() {
        return suc052;
    }

    public int getSuc054() {
        return suc054;
    }

    public int getSuc056() {
        return suc056;
    }

    public int getSuc058() {
        return suc058;
    }

    public int getSuc060() {
        return suc060;
    }

    public int getSuc062() {
        return suc062;
    }

    public int getSuc064() {
        return suc064;
    }

    public int getSuc066() {
        return suc066;
    }

    public int getSuc068() {
        return suc068;
    }

    public int getSuc070() {
        return suc070;
    }

    public int getSuc072() {
        return suc072;
    }

    public int getSuc074() {
        return suc074;
    }

    public int getSuc076() {
        return suc076;
    }

    public int getSuc078() {
        return suc078;
    }

    public int getSuc080() {
        return suc080;
    }

    public int getSuc082() {
        return suc082;
    }

    public int getSuc084() {
        return suc084;
    }

    public int getSuc086() {
        return suc086;
    }

    public int getSuc088() {
        return suc088;
    }

    public int getSuc090() {
        return suc090;
    }

    public int getSuc092() {
        return suc092;
    }

    public int getSuc094() {
        return suc094;
    }

    public int getSuc096() {
        return suc096;
    }

    public int getSuc098() {
        return suc098;
    }

    public int getSuc100() {
        return suc100;
    }

    public int getFal002() {
        return fal002;
    }

    public int getFal004() {
        return fal004;
    }

    public int getFal006() {
        return fal006;
    }

    public int getFal008() {
        return fal008;
    }

    public int getFal010() {
        return fal010;
    }

    public int getFal012() {
        return fal012;
    }

    public int getFal014() {
        return fal014;
    }

    public int getFal016() {
        return fal016;
    }

    public int getFal018() {
        return fal018;
    }

    public int getFal020() {
        return fal020;
    }

    public int getFal022() {
        return fal022;
    }

    public int getFal024() {
        return fal024;
    }

    public int getFal026() {
        return fal026;
    }

    public int getFal028() {
        return fal028;
    }

    public int getFal030() {
        return fal030;
    }

    public int getFal032() {
        return fal032;
    }

    public int getFal034() {
        return fal034;
    }

    public int getFal036() {
        return fal036;
    }

    public int getFal038() {
        return fal038;
    }

    public int getFal040() {
        return fal040;
    }

    public int getFal042() {
        return fal042;
    }

    public int getFal044() {
        return fal044;
    }

    public int getFal046() {
        return fal046;
    }

    public int getFal048() {
        return fal048;
    }

    public int getFal050() {
        return fal050;
    }

    public int getFal052() {
        return fal052;
    }

    public int getFal054() {
        return fal054;
    }

    public int getFal056() {
        return fal056;
    }

    public int getFal058() {
        return fal058;
    }

    public int getFal060() {
        return fal060;
    }

    public int getFal062() {
        return fal062;
    }

    public int getFal064() {
        return fal064;
    }

    public int getFal066() {
        return fal066;
    }

    public int getFal068() {
        return fal068;
    }

    public int getFal070() {
        return fal070;
    }

    public int getFal072() {
        return fal072;
    }

    public int getFal074() {
        return fal074;
    }

    public int getFal076() {
        return fal076;
    }

    public int getFal078() {
        return fal078;
    }

    public int getFal080() {
        return fal080;
    }

    public int getFal082() {
        return fal082;
    }

    public int getFal084() {
        return fal084;
    }

    public int getFal086() {
        return fal086;
    }

    public int getFal088() {
        return fal088;
    }

    public int getFal090() {
        return fal090;
    }

    public int getFal092() {
        return fal092;
    }

    public int getFal094() {
        return fal094;
    }

    public int getFal096() {
        return fal096;
    }

    public int getFal098() {
        return fal098;
    }

    public int getFal100() {
        return fal100;
    }
}
