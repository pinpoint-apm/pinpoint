package com.nhn.pinpoint.web.alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;
import com.nhn.pinpoint.web.alarm.filter.FailureCountFilter;
import com.nhn.pinpoint.web.alarm.filter.FailureRatesFilter;
import com.nhn.pinpoint.web.alarm.filter.SlowCountFilter;
import com.nhn.pinpoint.web.alarm.filter.SlowRatesFilter;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author koo.taejin
 */
public enum SubCategory {

	// 이후에 Filter가 늘어나게 되면 Decorator패턴으로 변경하는 것도 좋은 방법일듯함
	// 보낸 요청 받은 요청 
	FAIL_RATE("FAIL_RATE", 1, "%", MainCategory.REQUEST_SENDED) {
		@Override
		public AlarmCheckFilter createAlarmFilter(Application application, MainCategory parent, AlarmRuleResource rule) {
			AlarmCheckFilter filter = null;
			if (MainCategory.REQUEST_SENDED == parent) {
				filter = new FailureRatesFilter(application);
			}

			if (filter != null) {
				filter.initialize(rule);
			}
			
			return filter;
		}
	},
	FAIL_COUNT("FAIL_COUNT", 2, " ", MainCategory.REQUEST_SENDED) {	
		@Override
		public AlarmCheckFilter createAlarmFilter(Application application, MainCategory parent, AlarmRuleResource rule) {
			AlarmCheckFilter filter = null;
			if (MainCategory.REQUEST_SENDED == parent) {
				filter = new FailureCountFilter(application);
			}

			if (filter != null) {
				filter.initialize(rule);
			}

			return filter;
		}
	},
	SLOW_RATE("SLOW_RATE", 3, "%", MainCategory.REQUEST_SENDED) {
		@Override
		public AlarmCheckFilter createAlarmFilter(Application application, MainCategory parent, AlarmRuleResource rule) {
			AlarmCheckFilter filter = null;
			if (MainCategory.REQUEST_SENDED == parent) {
				filter = new SlowRatesFilter(application);
			}

			if (filter != null) {
				filter.initialize(rule);
			}

			return filter;
		}
	},
	SLOW_COUNT("SLOW_COUNT", 4, " ", MainCategory.REQUEST_SENDED) {
		@Override
		public AlarmCheckFilter createAlarmFilter(Application application, MainCategory parent, AlarmRuleResource rule) {
			AlarmCheckFilter filter = null;
			if (MainCategory.REQUEST_SENDED == parent) {
				filter = new SlowCountFilter(application);
			}

			if (filter != null) {
				filter.initialize(rule);
			}

			return filter;
		}
	};

	private final String name;
	private final int code;
	private final List<MainCategory> parentSupportCategoryList;
	private final String unit;

	private SubCategory(String name, int code, String unit, MainCategory firstParentSupportCategory, MainCategory... otherParentSupportCategories) {
		this.name = name;
		this.code = code;
		this.unit = unit;

		parentSupportCategoryList = new ArrayList<MainCategory>();

		parentSupportCategoryList.add(firstParentSupportCategory);

        Collections.addAll(parentSupportCategoryList, otherParentSupportCategories);
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

	public String getUnit() {
		return unit;
	}

	private List<MainCategory> getParentSupportCategoryList() {
		return parentSupportCategoryList;
	}

	AlarmFilter createAlarmFilter(Application application, AlarmRuleResource rule) throws Exception {
		List<MainCategory> parentSupportCategoryList = getParentSupportCategoryList();
		if (parentSupportCategoryList.size() == 1) {
			return createAlarmFilter(application, parentSupportCategoryList.get(0), rule);
		} else {
			throw new Exception("Ambiguous ParentCategory Exception");
		}
	}
	
	public abstract AlarmCheckFilter createAlarmFilter(Application application, MainCategory parent, AlarmRuleResource rule);

	public static SubCategory getValue(String value) {
		SubCategory[] categories = SubCategory.values();
		for (SubCategory category : categories) {
			if (category.getName().equalsIgnoreCase(value)) {
				return category;
			}
		}
		
		return null;
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
