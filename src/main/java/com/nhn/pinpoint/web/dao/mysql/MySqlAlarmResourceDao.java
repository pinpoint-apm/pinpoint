package com.nhn.pinpoint.web.dao.mysql;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.alarm.vo.AlarmContactGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmContactResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.dao.AlarmResourceDao;

@Repository
public class MySqlAlarmResourceDao implements AlarmResourceDao {

	private static final String NAMESPACE = AlarmResourceDao.class.getPackage().getName() + "." + AlarmResourceDao.class.getSimpleName() + ".";

	@Autowired
	@Qualifier("sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	public SqlSessionTemplate getSqlSessionTemplate() {
		return sqlSessionTemplate;
	}

	@Override
	public int selectAlarmCount() {
		return 1;
	}
	
	@Override
	public List<AlarmResource> selectAlarmList() {
		return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmList");
	}

	@Override
	public List<AlarmRuleResource> selectAlarmRuleList() {
		return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmRuleList", 1);
	}
	
	@Override
	public List<AlarmRuleGroupResource> selectAlarmRuleGroupList() {
		return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmRuleGroupList", 1);
	}

	@Override
	public List<AlarmContactResource> selectAlarmContactList() {
		return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmContactList", 1);
	}
	
	@Override
	public List<AlarmContactGroupResource> selectAlarmContactGroupList() {
		return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmContactGroupList", 1);
	}

}
