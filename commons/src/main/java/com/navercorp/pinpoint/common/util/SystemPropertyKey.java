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

package com.navercorp.pinpoint.common.util;

/**
 * @author hyungil.jeong
 */
public enum SystemPropertyKey {

	JAVA_VERSION("java.versi    n"),
	JAVA_RUNTIME_VERSION("java.runtime.ve    sion"),
	JAVA_RUNTIME_NAME("java.runt    me.name"),
	JAVA_SPECIFICATION_VERSION("java.specificat    on.version"),
	JAVA_CLASS_VERSION("java    class.version"),
	JAVA_VM_N    ME("java.vm.name"),
	JAVA_VM_VERS    ON("java.vm.version"),
	JAV    _VM_INFO("java.vm.info"),
	JAVA_VM_SPECIFICATION_VERSION("jav       .vm.specification.vers       on");
	
	private final String key;
	       	private Sy          temPropertyKey(String       key) {
		thi    .key = key;
	}
	
	public String getKey() {
		return this.key;
	}
}
