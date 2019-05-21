/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;


/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchPluginConfig {

    private final boolean elasticsearchEnabled;
	private final boolean recordResult ;
	private final boolean recordArgs ;
	private final boolean recordDsl ;
	private final boolean recordESVersion ;
	private final boolean recordResponseHandler ;

	private final int maxDslSize;
    public boolean isEnabled() {
		return elasticsearchEnabled;
	}



	public ElasticsearchPluginConfig(ProfilerConfig profilerConfig) {
    	if(profilerConfig != null) {
			this.elasticsearchEnabled = profilerConfig.readBoolean("profiler.elasticsearchbboss.enabled", true);
			recordResult = profilerConfig.readBoolean("profiler.elasticsearchbboss.recordResult",false);
			recordArgs = profilerConfig.readBoolean("profiler.elasticsearchbboss.recordArgs",true);
			recordDsl =  profilerConfig.readBoolean("profiler.elasticsearchbboss.recordDsl",true);
			maxDslSize =  profilerConfig.readInt("profiler.elasticsearchbboss.maxDslSize",ElasticsearchConstants.maxDslSize);
			recordResponseHandler =  profilerConfig.readBoolean("profiler.elasticsearchbboss.recordResponseHandlerClass",false);
			recordESVersion = profilerConfig.readBoolean("profiler.elasticsearchbboss.recordESVersion",true);

		}
		else {
			this.elasticsearchEnabled = false;
			recordResult = false;
			recordArgs = true;
			recordDsl =  true;
			maxDslSize =  ElasticsearchConstants.maxDslSize;
			recordResponseHandler =  false;
			recordESVersion = true;
		}
    }

     

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ElasticsearchBBossPluginConfig{");
        sb.append("elasticsearchBBossEnabled=").append(elasticsearchEnabled);
		sb.append(",recordResult=").append(recordResult);
		sb.append(",recordArgs=").append(recordArgs);
		sb.append(",recordDsl=").append(recordDsl);
		sb.append(",maxDslSize=").append(maxDslSize);
		sb.append(",recordResponseHandler=").append(recordResponseHandler);
		sb.append(",recordESVersion=").append(recordESVersion);
        sb.append('}');
        return sb.toString();
    }

	public boolean isRecordResult() {
		return recordResult;
	}

	public boolean isRecordArgs() {
		return recordArgs;
	}

	public boolean isRecordDsl() {
		return recordDsl;
	}

	public boolean isRecordESVersion() {
		return recordESVersion;
	}

	public boolean isRecordResponseHandler() {
		return recordResponseHandler;
	}

	public int getMaxDslSize() {
		return maxDslSize;
	}
}
