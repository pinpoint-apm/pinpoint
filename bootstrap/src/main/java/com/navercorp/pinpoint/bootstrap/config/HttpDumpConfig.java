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

    public static HttpDumpConfig getDefault()       {
		HttpDumpConfig config = new HttpDumpCo       fig();

		config.setDump       ookie(false);
		config.setCookieDumpType(       umpType.EXCEPTION);
		config.setCookieSampler(SimpleSamplerFactory       createSampler(false, 1));
	       config.setCookieDumpSize       128);

		config.setDumpEntity(false);
		c       nfig.setEntityDumpType(DumpType.EXCEPTION);
		config.setEntitySamp       er(SimpleSamplerFactory.cre       teSampler(false, 1));
	       config.setEntityDumpSize(128);

		config       setDumpParam(false);
		config.setParamDumpType(DumpType.EXCEPTION       ;
		config.setParamSampler       SimpleSamp        rFactory.createSampler(false, 1))
		config.setParamDumpSize(128);

		return config;
    }

	private boolean dumpCookie = f    lse;
	private DumpType coo    ieDumpType = DumpType.EXC    PTION;
	private SimpleSampler     ookieSampler;
	private int cookieD    mpSize;

	private boolean     umpEntity;
	private Dump    ype entityDumpType;
	private     impleSampler entitySampler;
	priv    te int entityDumpSize;

	    rivate boolean dumpParam;
	pr       vate DumpType         ramDumpType;
	private SimpleSampler paramSamp       er;
	private int paramDum        ize;

	public boolean isDumpCookie(        {
		return dumpCo        ie;
	}

	public void setDumpCookie(boolean dumpCookie)       {
		this.dumpCookie = dumpCookie;        }

	public DumpType getCookieDumpType()       {
		return cookie        mpType;
	}

	public void setCookieDumpType(DumpType cooki       DumpType) {
		this.cookieDumpTy         = cookieDumpType;
	}

	public       SimpleSampler getC        kieSampler() {
		return cookieSampler;
	}

	publi        void setCookieSampler(SimpleSamp        r cookieSampler) {
		this.coo       ieSampler = co        ieSampler;
	}

	public int getCookieDumpSize(        {
		return cookieDumpSiz
	}

	public void setCookieDumpSize       int cookieDumpSize        {
		this.cookieDumpSize = cookieDumpSize;
	}

	public        oolean isDumpEntity() {
		return         mpEntity;
	}

	public void setDumpEntit       (boolean dumpEnti        ) {
		this.dumpEntity = dumpEntity;
	}

	public DumpType        etEntityDumpType() {
		return e        ityDumpType;
	}

	public void        etEntityDumpType(D        pType entityDumpType) {
		this.entityDumpType = e       tityDumpType;
	}

	public SimpleS        pler getEntitySampler() {
		       eturn entityS        pler;
	}

	public void setEntitySampler(Sim       leSampler entitySampler        {
		this.entitySampler = entitySam       ler;
	}

	public         t getEntityDumpSize() {
		return entityDumpSize;
	}
       	public void setEntityDumpSize(        t entityDumpSize) {
		this.entityDumpS       ze = entityDumpS        e;
	}

	public boolean isDumpParam() {
		return dumpPar       m;
	}

	public void setDumpPa        m(boolean dumpParam) {
		this       dumpParam = dumpP        am;
	}

	public DumpType getParamDumpType() {
	       return paramDumpType;
	}

	publ    c void setParamDumpType(DumpType paramDumpType) {
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
