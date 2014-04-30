package com.nhn.pinpoint.web.mapper.mysql;

import java.util.List;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;

public interface AlarmRuleInfoMapper {
	
//	 where alrm_rule_grp_id=#{id}
	final String GET_RULE_LIST = "SELECT * FROM alrm_rule_info";

	
	@Select(GET_RULE_LIST)
	@Results(value = {
		@Result(property="id"),
		@Result(property="alarmRuleGroupId",  column="alrm_rule_grp_id"),
		@Result(property="alarmRuleSubCategoryId",  column="alrm_rule_sb_catg_id"),
		@Result(property="agentId",  column="agt_id"),
		@Result(property="thresholdRule",  column="thrhd_rule"),
		@Result(property="compareRule",  column="cmpr_rule"),
		@Result(property="continuosTime",  column="cntu_tm"),
		@Result(property="alarmRuleName",  column="alrm_rule_nm"),
		@Result(property="alarmRuleDescrption",  column="alrm_rule_desc")
	})
	List<AlarmRuleResource> selectAlarmRuleList();
	
}
