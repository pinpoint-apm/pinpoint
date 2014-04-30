package com.nhn.pinpoint.web.dao.mysql;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.dao.AlarmRuleInfoDao;
import com.nhn.pinpoint.web.mapper.mysql.AlarmRuleInfoMapper;

@Repository
public class MySqlAlarmRuleInfoDao implements AlarmRuleInfoDao {

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
	public List<AlarmRuleResource> selectAlarmRuleList() {
		AlarmRuleInfoMapper mapper = getSqlSessionTemplate().getMapper(AlarmRuleInfoMapper.class);
		return mapper.selectAlarmRuleList();
	}
	
}
