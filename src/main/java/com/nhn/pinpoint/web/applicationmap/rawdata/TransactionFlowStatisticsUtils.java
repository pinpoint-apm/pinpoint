package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;

@Deprecated
public class TransactionFlowStatisticsUtils {
	public static String makeId(String from, ServiceType fromServiceType, String to, ServiceType toServiceType) {
		return from + fromServiceType + to + toServiceType;
	}
}
