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

package com.navercorp.pinpoint.common.util;

/**
 * @author hyungil.jeong
 */
public enum JvmVersion {
    JAVA_5(1.5, 49    ,
	JAVA_6(1.6,    50),
	JAVA_7(1    7, 51),
	JAVA_    (1.8, 52),
	UNSUPP       RTED(-1, -1);
	
	private f    nal double version;
	private        inal int classVersion;
	
	private JvmVersion(double       version, int classV       rsion) {
		this.version = ver          ion;
		this.classVersion = classVersion;
       }
	
	public boolean onOrAfter(JvmVersion other           {
		if             (this == UNSUPPORTED || other == UNSUPPORTED) {          			return false;
		}
		return this == other || this.versio                   other.version;
	}
	
	public static JvmVers          on getFromVersion(String        avaVersion) {
		try {
			double          version = Dou                le.parseDouble(javaVersion);
			return getFromVersion(ve       sion);
		} catch (NumberFormatException e) {          			return UNSUPPORTED;
		}
	}
	
	public static Jvm             ersion                       etFromVersion(double          javaVersion) {
		for (JvmVersion version : JvmVersion.values(       ) {
			if (Double.compare(version.version, j          vaVersion) == 0) {
				return version
			}
	                      }
		return JvmVersio    .UNSUPPORTED;
	}
	
	public static JvmVersion getFromClassVersion(int classVersion) {
		for (JvmVersion version : JvmVersion.values()) {
			if (version.classVersion == classVersion) {
				return version;
			}
		}
		return JvmVersion.UNSUPPORTED;
	}
}
