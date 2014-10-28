package com.nhn.pinpoint.profiler.modifier.orm.ibatis.filter;

import java.lang.reflect.Modifier;

import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;

/**
 * @author Hyun Jeong
 */
public abstract class IbatisMethodFilter implements MethodFilter {

	private static final boolean TRACK = false;
	private static final boolean DO_NOT_TRACK = true;

	protected abstract boolean shouldTrackMethod(String methodName);

	@Override
	public boolean filter(MethodInfo ctMethod) {
		final int modifiers = ctMethod.getModifiers();
		if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
			return DO_NOT_TRACK;
		}
		return filterApiForTracking(ctMethod);
	}

	private boolean filterApiForTracking(MethodInfo ctMethod) {
		if (!shouldTrackMethod(ctMethod.getName())) {
			return DO_NOT_TRACK;
		}

		final int parameterIndexToMatch = 0; // 0-based index
		String[] parameterTypes = ctMethod.getParameterTypes();
		if (parameterTypes != null && parameterTypes.length > 0) {
		    return parameterTypeMatches(parameterTypes, parameterIndexToMatch, String.class);
		} else {
		    return TRACK;
		}
	}

	private boolean parameterTypeMatches(final String[] parameterTypes, final int parameterIndex, final Class<?> parameterType) {
		if (parameterTypes == null || parameterTypes.length <= parameterIndex) {
			return DO_NOT_TRACK;
		}
		if (parameterType.getName().equals(parameterTypes[parameterIndex])) {
			return TRACK;
		}
		return DO_NOT_TRACK;
	}

}
