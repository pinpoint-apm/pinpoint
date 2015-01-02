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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class HttpDumpConfig {

    public static HttpDumpConfig getDefault() {
        HttpDumpConfig config = new HttpDumpConfig();

        config.setDumpCookie(false);
        config.setCookieDumpType(DumpType.EXCEPTION);
        config.setCookieSampler(SimpleSamplerFactory.createSampler(false, 1));
        config.setCookieDumpSize(128);

        config.setDumpEntity(false);
        config.setEntityDumpType(DumpType.EXCEPTION);
        config.setEntitySampler(SimpleSamplerFactory.createSampler(false, 1));
        config.setEntityDumpSize(128);

        config.setDumpParam(false);
        config.setParamDumpType(DumpType.EXCEPTION);
        config.setParamSampler(SimpleSamplerFactory.createSampler(false, 1));
        config.setParamDumpSize(128);

        return config;
    }

    private boolean dumpCookie = false;
    private DumpType cookieDumpType = DumpType.EXCEPTION;
    private SimpleSampler cookieSampler;
    private int cookieDumpSize;

    private boolean dumpEntity;
    private DumpType entityDumpType;
    private SimpleSampler entitySampler;
    private int entityDumpSize;

    private boolean dumpParam;
    private DumpType paramDumpType;
    private SimpleSampler paramSampler;
    private int paramDumpSize;

    public boolean isDumpCookie() {
        return dumpCookie;
    }

    public void setDumpCookie(boolean dumpCookie) {
        this.dumpCookie = dumpCookie;
    }

    public DumpType getCookieDumpType() {
        return cookieDumpType;
    }

    public void setCookieDumpType(DumpType cookieDumpType) {
        this.cookieDumpType = cookieDumpType;
    }

    public SimpleSampler getCookieSampler() {
        return cookieSampler;
    }

    public void setCookieSampler(SimpleSampler cookieSampler) {
        this.cookieSampler = cookieSampler;
    }

    public int getCookieDumpSize() {
        return cookieDumpSize;
    }

    public void setCookieDumpSize(int cookieDumpSize) {
        this.cookieDumpSize = cookieDumpSize;
    }

    public boolean isDumpEntity() {
        return dumpEntity;
    }

    public void setDumpEntity(boolean dumpEntity) {
        this.dumpEntity = dumpEntity;
    }

    public DumpType getEntityDumpType() {
        return entityDumpType;
    }

    public void setEntityDumpType(DumpType entityDumpType) {
        this.entityDumpType = entityDumpType;
    }

    public SimpleSampler getEntitySampler() {
        return entitySampler;
    }

    public void setEntitySampler(SimpleSampler entitySampler) {
        this.entitySampler = entitySampler;
    }

    public int getEntityDumpSize() {
        return entityDumpSize;
    }

    public void setEntityDumpSize(int entityDumpSize) {
        this.entityDumpSize = entityDumpSize;
    }

    public boolean isDumpParam() {
        return dumpParam;
    }

    public void setDumpParam(boolean dumpParam) {
        this.dumpParam = dumpParam;
    }

    public DumpType getParamDumpType() {
        return paramDumpType;
    }

    public void setParamDumpType(DumpType paramDumpType) {
        this.paramDumpType = paramDumpType;
    }

    public SimpleSampler getParamSampler() {
        return paramSampler;
    }

    public void setParamSampler(SimpleSampler paramSampler) {
        this.paramSampler = paramSampler;
    }

    public int getParamDumpSize() {
        return paramDumpSize;
    }

    public void setParamDumpSize(int paramDumpSize) {
        this.paramDumpSize = paramDumpSize;
    }
}
