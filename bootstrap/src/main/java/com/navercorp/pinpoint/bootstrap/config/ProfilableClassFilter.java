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

import java.util.HashSet;
import java.util.Set;

/**
 * @author emeroad
 */
public class ProfilableClassFilter implements Filter<String> {

    private final Set<String> profileInclude = new HashSet<String>(    ;
	private final Set<String> profileIncludeSub = new HashSet<String    ();

	public ProfilableClassFilter(String profilable       lass) {
		if (profilableClass == null || profilableClas          .             sEmpty()) {
			return;
		}
		String[] clas       Name = profilableClass.spl          t(",");
		for (Stri             g str : className) {
			if (str.endsWith(".*")) {
				this.profileIncludeSub.ad          (s             r.substring(0, str.length() - 2).repla             e('.', '/') + "/");
			}                          lse {
				String replace = str.trim().replace('.', '/');
				this.profileInclude        dd(replace);
			}    		}
	}

    /    *
	 * T    DO remove this. Added this method to te       t the "call stack view" on a test ser          er
	 *       	 *           param className
	 * @return
	 */
	@Override
	public boolean filter(String cl          ssName) {
		if (profileInclude.c             ntains(className)) {
			re                ur                                         true;
    	} else {
			final Strin        packageName = className.substring(0, className.lastIndexOf("/")         1);
			for (String pkg : profileIncludeSub) {
	       		if (packageName.startsWith(pkg)) {
					return true;
	       		}
			}

		return false;
    }


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ProfilableClassFilter{");
		sb.append("profileInclude=").append(profileInclude);
		sb.append(", profileIncludeSub=").append(profileIncludeSub);
		sb.append('}');
		return sb.toString();
	}
}
