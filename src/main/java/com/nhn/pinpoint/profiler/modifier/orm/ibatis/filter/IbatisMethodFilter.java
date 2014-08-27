package com.nhn.pinpoint.profiler.modifier.orm.ibatis.filter;

import java.lang.reflect.Modifier;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;

/**
 * @author Hyun Jeong
 */
public abstract class IbatisMethodFilter implements MethodFilter {

	private static final boolean TRACK = false;
	private static final boolean DO_NOT_TRACK = true;

	protected abstract boolean shouldTrackMethod(String methodName);

	@Override
	public boolean filter(CtMethod ctMethod) {
		final int modifiers = ctMethod.getModifiers();
		if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
			return DO_NOT_TRACK;
		}
		return filterApiForTracking(ctMethod);
	}

	private boolean filterApiForTracking(CtMethod ctMethod) {
		if (!shouldTrackMethod(ctMethod.getName())) {
			return DO_NOT_TRACK;
		}
		try {
			final int parameterIndexToMatch = 0; // 0-based index
			CtClass[] parameterTypes = ctMethod.getParameterTypes();
			if (parameterTypes != null && parameterTypes.length > 0) {
				return parameterTypeMatches(parameterTypes, parameterIndexToMatch, String.class);
			} else {
				return TRACK;
			}
		} catch (NotFoundException e) {
			return DO_NOT_TRACK;
		}
	}

	private boolean parameterTypeMatches(final CtClass[] parameterTypes, final int parameterIndex, final Class<?> parameterType) {
		if (parameterTypes == null || parameterTypes.length <= parameterIndex) {
			return DO_NOT_TRACK;
		}
		if (parameterType.getName().equals(parameterTypes[parameterIndex].getName())) {
			return TRACK;
		}
		return DO_NOT_TRACK;
	}

}
