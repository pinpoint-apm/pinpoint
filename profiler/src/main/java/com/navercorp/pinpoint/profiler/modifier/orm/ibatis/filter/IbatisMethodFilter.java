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

package com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter;

import java.lang.reflect.Modifier;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;

/**
 * @author Hyun Jeong
 */
public abstract class IbatisMethodFilter implements MethodFilter {

    private static final boolean TRACK = fals    ;
	private static final boolean DO_NOT_TRACK = t    ue;

	protected abstract boolean shouldTrackMethod(String metho    Name);
    	@Override
	public boolean filter(MethodIn       o ctMethod) {
		final int modifiers = ctMe       hod.getModifiers();
		if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modif          er.isNative(mo             ifiers)) {
			return DO_NOT_TRAC
		}
		return filterApiForTracking(ctMethod);
	}

	privat        boolean filterApiForTracking(MethodInfo           tMethod) {
		i              (!shouldTrackMethod(ctMethod.getName())) {
			r       turn DO_NOT_TRACK;
		}

		final int parameterIndexT       Match = 0; // 0-based index
		String[] parameterTypes         ctMethod.getParameterTypes();
		if (parameterTypes != null && parameterTypes.len       th >       0) {
		    re             urn parameterTypeMatches(parameterTypes, parameterIndexToMatch, String.class);
		} else {
		    return TRACK;
		}
	}

	pri       ate boolean parameterTypeMatches(final String[] parameterTypes, fina           int parameter             ndex, final Class<?> parameterType) {
		if (parameterTypes == n          ll || p             rameterTypes.l    ngth <= parameterIndex) {
			return DO_NOT_TRACK;
		}
		if (parameterType.getName().equals(parameterTypes[parameterIndex])) {
			return TRACK;
		}
		return DO_NOT_TRACK;
	}

}
