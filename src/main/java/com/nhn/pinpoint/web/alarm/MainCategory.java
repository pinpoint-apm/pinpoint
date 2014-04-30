package com.nhn.pinpoint.web.alarm;

import java.util.Locale;

public enum MainCategory {

	REQUEST("REQUEST", 1);

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
