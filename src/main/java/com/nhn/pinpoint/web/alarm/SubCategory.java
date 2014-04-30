package com.nhn.pinpoint.web.alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum SubCategory {

	RATE_FAIL("RATE_FAIL", 1, MainCategory.REQUEST) {
		@Override
		public AlarmFilter createAlarmFilter(MainCategory parent) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	COUNT_FAIL("COUNT_FAIL", 2, MainCategory.REQUEST) {
		@Override
		public AlarmFilter createAlarmFilter(MainCategory parent) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	private final String name;
	private final int code;
	private final List<MainCategory> parentSupportCategoryList;

	private SubCategory(String name, int code, MainCategory firstParentSupportCategory, MainCategory... otherParentSupportCategories) {
		this.name = name;
		this.code = code;

		parentSupportCategoryList = new ArrayList<MainCategory>();

		parentSupportCategoryList.add(firstParentSupportCategory);

		for (MainCategory category : otherParentSupportCategories) {
			parentSupportCategoryList.add(category);
		}
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

	private List<MainCategory> getParentSupportCategoryList() {
		return parentSupportCategoryList;
	}

	AlarmFilter createAlarmFilter() throws Exception {
		List<MainCategory> parentSupportCategoryList = getParentSupportCategoryList();
		if (parentSupportCategoryList.size() == 1) {
			return createAlarmFilter(parentSupportCategoryList.get(0));
		} else {
			throw new Exception("Ambiguous ParentCategory Exception");
			// 
		}
	}
	
	public abstract AlarmFilter createAlarmFilter(MainCategory parent);

	public static SubCategory getValue(String value) {
		return SubCategory.valueOf(value.toUpperCase(Locale.ENGLISH));
	}

	public static SubCategory getValue(int code) {
		for (SubCategory eachCategory : SubCategory.values()) {
			if (eachCategory.getCode() == code) {
				return eachCategory;
			}
		}

		return null;
	}

}
