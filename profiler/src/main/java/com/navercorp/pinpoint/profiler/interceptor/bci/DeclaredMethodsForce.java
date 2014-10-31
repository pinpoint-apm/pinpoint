package com.nhn.pinpoint.profiler.interceptor.bci;

import java.util.List;

/**
 * @author emeroad
 */
public class DeclaredMethodsForce {

	private final List<Method> declaredMethods;

	private final List<FailMethod> failDeclaredMethods;

	public DeclaredMethodsForce(List<Method> successMethod, List<FailMethod> failDeclaredMethods) {
		this.declaredMethods = successMethod;
		this.failDeclaredMethods = failDeclaredMethods;
	}

	public List<Method> getDeclaredMethods() {
		return declaredMethods;
	}

	public List<FailMethod> getFailDeclaredMethods() {
		return failDeclaredMethods;
	}

	public static class FailMethod extends Method {

		private final Throwable caused;

		public FailMethod(String methodName, String[] methodParams, Throwable caused) {
			super(methodName, methodParams);
			this.caused = caused;
		}

		public Throwable getCaused() {
			return caused;
		}

	}
}
