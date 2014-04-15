package com.nhn.pinpoint.profiler.modifier.orm.ibatis.filter;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;

/**
 * 
 * @author Hyun Jeong
 */
public class SqlMapClientMethodModifier implements MethodFilter {

	private static final boolean TRACK = false;
	private static final boolean DO_NOT_TRACK = true;

	private static final Set<String> WHITE_LIST_API = createWhiteListApi();

	private static final Set<String> createWhiteListApi() {
		return new HashSet<String>(Arrays.asList(IbatisInterfaceApi.sqlMapClientApis));
	}

	@Override
	public boolean filter(CtMethod ctMethod) {
		final int modifiers = ctMethod.getModifiers();
		if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
            return DO_NOT_TRACK;
        }
		return filterApiForTracking(ctMethod);
	}

	private boolean filterApiForTracking(CtMethod ctMethod) {
		if (WHITE_LIST_API.contains(ctMethod.getName()) == false) {
			return DO_NOT_TRACK;
		}
		try {
			final int parameterIndexToMatch = 0; // 0-based index
			CtClass[] parameterTypes = ctMethod.getParameterTypes();
			return parameterTypeMatches(parameterTypes, parameterIndexToMatch, String.class);
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
