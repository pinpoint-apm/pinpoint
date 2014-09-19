package com.nhn.pinpoint.collector.util;

import java.lang.management.ManagementFactory;

/**
 * @author koo.taejin <kr14910>
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
