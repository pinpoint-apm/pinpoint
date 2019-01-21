/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;


import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class SpanRecorderIT implements SpanRecorder {
	@Override
	public boolean canSampled() {
		return true;
	}

	@Override
	public boolean isRoot() {
		return true;
	}

	@Override
	public void recordStartTime(long startTime) {

	}

	@Override
	public void recordTime(boolean autoTimeRecoding) {

	}

	@Override
	public void recordError() {

	}

	@Override
	public void recordException(Throwable throwable) {

	}

	@Override
	public void recordException(boolean markError, Throwable throwable) {

	}

	@Override
	public void recordApiId(int apiId) {

	}

	@Override
	public void recordApi(MethodDescriptor methodDescriptor) {

	}

	@Override
	public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {

	}

	@Override
	public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {

	}

	@Override
	public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {

	}

	@Override
	public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {

	}

	@Override
	public void recordAttribute(AnnotationKey key, String value) {

	}

	@Override
	public void recordAttribute(AnnotationKey key, int value) {

	}

	@Override
	public void recordAttribute(AnnotationKey key, Object value) {

	}

	@Override
	public void recordServiceType(ServiceType serviceType) {

	}

	@Override
	public void recordRpcName(String rpc) {

	}

	@Override
	public void recordRemoteAddress(String remoteAddress) {

	}

	@Override
	public void recordEndPoint(String endPoint) {

	}

	@Override
	public void recordParentApplication(String parentApplicationName, short parentApplicationType) {

	}

	@Override
	public void recordAcceptorHost(String host) {

	}

	@Override
	public void recordLogging(LoggingInfo loggingInfo) {

	}

	@Override
	public void recordStatusCode(int statusCode) {

	}

	@Override
	public Object attachFrameObject(Object frameObject) {
		return new Object();
	}

	@Override
	public Object getFrameObject() {
		return new Object();
	}

	@Override
	public Object detachFrameObject() {
		return new Object();
	}
}
