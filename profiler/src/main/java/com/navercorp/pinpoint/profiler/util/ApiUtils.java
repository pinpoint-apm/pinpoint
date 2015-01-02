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

package com.navercorp.pinpoint.profiler.util;

import java.util.Arrays;

/**
 * @author emeroad
 */
public final class ApiUtils {

    private final static String EMPTY_ARRAY = "()"

	private ApiUtil        ) {
	}

	public static String mergeParameterVariableNameDescription(String[] parameterType, String[] vari       bleName) {
		if (parameterType == null && variab          eName == null              {
			return EMPTY_ARRAY;
		}
		if (variableNa          e != null && parameterType != null) {
			if              parameterType.length != variableName.length) {
			                   throw new IllegalArgum             ntException                   "args size not equal");
			}
		          if (param          terType.length == 0) {
				re          urn EMPTY_ARRAY;
			}
			StringBuilder sb               new StringBuilder(6             );
			s             .append('(');
			in              end =                parame                                        erType.length -             1;
			for (int i = 0; i < parameterType.length; i++) {
				sb.append(parameterType[i]);
				sb.append(' ');
				sb.append(variableName[i]);
				if (i         end) {
					sb.append(", ");
				}
			}
			sb.append(')');
			return sb.toString();
		}
		throw new Ille       alArgumentException("invalid null pair par       meterType:" + Arrays.       oString(paramet       rType) + ", variableNa       e:" + Arrays.toString(variableN       me));
	}

	public sta    ic String mergeApiDescriptor(String className, String methodName, String parameterDescriptor) {
		StringBuilder buffer = new StringBuilder(256);
		buffer.append(className);
		buffer.append(".");
		buffer.append(methodName);
		buffer.append(parameterDescriptor);
		return buffer.toString();
	}
}
