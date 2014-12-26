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

package com.navercorp.pinpoint.collector.util;

import java.lang.management.ManagementFactory;

/**
 * @author koo.taejin
 */
public final class CollectorUtils {

	private CollectorUtils() {
	}

	public static String getServerIdentifier() {

		// 해당 값이 유일한 값이 아닌 경우 MAC주소나 IP주소 등으로 변경할 예정
		// 요렇게 하면 pid@hostname 으로 나옴 (localhost 요런놈은 겹칠 가능성이 존재함)
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
}
