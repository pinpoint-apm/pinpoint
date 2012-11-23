package com.profiler.common;

import com.profiler.common.dto.thrift.Span;

@Deprecated
public enum ServiceNames {

	UNKNOWN(0) {
		@Override
		public String getApplicationName(Span span) {
			return "UNKNOWN-APPLICATION";
		}
	},
	TOMCAT(1) {
		@Override
		public String getApplicationName(Span span) {
			return "TOMCAT";
		}
	},
	ARCUS(2) {
		@Override
		public String getApplicationName(Span span) {
			return "ARCUS";
		}
	},
	MYSQL(3) {
		@Override
		public String getApplicationName(Span span) {
			return "MYSQL";
		}
	};

	private int code;

	ServiceNames(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}

//	public static ServiceNames findApplicationType(Span span) {
//		// int code = span.getTerminalType();
//		int code = -1;
//
//		ServiceNames[] values = ServiceNames.values();
//		for (ServiceNames value : values) {
//			if (value.code == code) {
//				return value;
//			}
//		}
//		return UNKNOWN;
//	}

	public abstract String getApplicationName(Span span);
}
