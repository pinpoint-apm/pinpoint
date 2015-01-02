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

package com.navercorp.pinpoint.thrift.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public enum TCommandTypeVersion {

    // Match with agent versi    n
	V_1_0_2_SNAPSHOT("1.0.2-SNAPSHOT", TCommandType.RESULT, TCommandType.THREAD_D    MP),
	V_1_0_2("1.0.2", V_1_0_2_SN    PSHOT),
	V_1_0_3_SNAPSHOT("1.0.3-SNAPSHOT", V_1_0_2, TCommandType.ECHO, TCommandType.TRANSFER, TCommandType.THREAD_DUMP    RESPONSE),
	V_1_0_3("1.0.3", V_1_    _3_SNAPSHOT),
	V_1_0_4_SNAPSHOT("1.0.4-SNA       SHOT", V_1_0_3),

	UNKNOWN("UNKNOWN");

	private    final String versionName;
	private final List<TCommandType> supportCommandList = ne     ArrayList<TCommandType>();

	private TCommandTypeVersion(String versionName, TCommandTypeVersion version, TComma       dType... supportCommandArra             ) {
		this.versionName = versionName;
		
		for (TCommandType s          pportCommand : version.getSupport             ommandList()) {
			supportCommandList.add(supportCom          and);
		}

		for (TCommandType support             ommand : supportCommandArray) {
			getSupportCommandList().add(supportCommand);
		}       	}

	private TCommandTypeVer       ion(String versionName, TCommandType... supportComman          Array) {
		this.versionName = versionN             me;

		for (TCommandType supportCommand : suppor       CommandArray) {
			get          upportCommandList().add(supportCommand);
		}
       }

	public List<TC          mmandTy                   e> getSupportCommandList() {
		return suppor          CommandList;
	}
	
	p             b                   ic boolean isSupportCommand(TBase command)             {
		                      f (c        mand == null) {
			return fals       ;
		}
		
		for           TCommandType eachCommand : supportCommandList) {
			if (eac       Command == null) {          				continue;
			}

			if (eachCommand.getClazz() ==                    ommand.getClass()) {
				return true;
			}
		}

		return fals          ;
	}

	public String getVersionName() {
		ret             rn versionN                            me
	}
	
	public static TCommandTypeVersion getVersion(String version) {
		if (version == null) {
			throw new NullPointerException("version may not be null.");
		}
		
		for (TCommandTypeVersion versionType : TCommandTypeVersion.values()) {
			if (versionType.getVersionName().equals(version)) {
				return versionType;
			}
		}
		
		return UNKNOWN;
	}

}
