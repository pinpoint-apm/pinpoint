package com.nhn.pinpoint.web.alarm;

import java.util.Locale;

public enum MainCategory {

	// 받은 요청, 보낸 요청 등으로 나누면 될듯 
	REQUEST_SENDED("REQUEST_SENDED", 1),
	REQUEST_RECEIVED("REQUEST_RECEIVED", 2)
	;

	private final String name;
	private final int code;

	private MainCategory(String name, int code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

	public static MainCategory getValue(String value) {
		return MainCategory.valueOf(value.toUpperCase(Locale.ENGLISH));
	}

	public static MainCategory getValue(int code) {
		for (MainCategory eachCategory : MainCategory.values()) {
			if (eachCategory.getCode() == code) {
				return eachCategory;
			}
		}

		return null;
	}

}
