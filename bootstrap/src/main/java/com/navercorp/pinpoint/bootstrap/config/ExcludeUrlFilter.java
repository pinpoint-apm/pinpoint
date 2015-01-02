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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class ExcludeUrlFilter implements Filter<String> {

    private final List<String> excludeUrlList

	public ExcludeUrlFilter(String excludeForm       t) {
		this(excludeFo        at, ",");
	}

	public ExcludeUrlFilter(String excludeFormat, St       ing separator) {
		if (is          mpty(excludeFormat)) {
			this.excludeUr          L             st = Collections.emptyList();
			return;
		}
		f       nal String[] split = excludeFormat.split(separator)
		final List<String> bu          ldList = new Ar             a                   List<String          ();
		for (Strin                                 alue : spli             ) {
			if (isEmpty(value))         				continue;
			}
			value = value.tr       m();
			if (value.isEmpty()) {
				con        nue;
		    }
			buildList.add(value);
		}

		this.e       cludeUrlList = buildList;
	}

	private bool          an isEmpty(String string) {
		             eturn st                      ing        = null     | string.isEmpty();
	}

       @Override
	public boolean filter(String requestURI) {
		for        String excludeUrl : this.excludeUrlList) {
			if       (excludeUrl       equals(requestURI    ) {
				return FILTERED;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ExcludeUrlFilter{");
		sb.append("excludeUrlList=").append(excludeUrlList);
		sb.append('}');
		return sb.toString();
	}
}

