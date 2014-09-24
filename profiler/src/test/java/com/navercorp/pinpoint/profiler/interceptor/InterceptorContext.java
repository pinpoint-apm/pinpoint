package com.nhn.pinpoint.profiler.interceptor;

import java.util.Arrays;

/**
 * @author emeroad
 */
@Deprecated
public class InterceptorContext {
	private Object target;
	private String className;
	private String methodName;
	private Object[] parameter;
	private Object value;

	private Object result;

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getParameter() {
		return parameter;
	}

	public void setParameter(Object[] parameter) {
		this.parameter = parameter;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "InterceptorContext{" + "target=" + target + ", className='" + className + '\'' + ", methodName='" + methodName + '\'' + ", parameter=" + (parameter == null ? null : Arrays.asList(parameter)) + ", value=" + value + ", result=" + result + '}';
	}
}
