package com.nhn.pinpoint.web.dao.mysql;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.vo.AlarmEmp;
import com.nhn.pinpoint.web.alarm.vo.Rule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class MySqlAlarmResourceDaoTest {

    @Autowired
    MySqlAlarmResourceDao dao;
    
    @Test
    public void getAlarmRules() {
        List<Rule> rules = dao.selectAppRule("minwoo_tomcat");
        assertNotNull(rules);
        assertTrue(rules.size() > 0);
    }
    
    @Test
    public void selectAlarmGroupList() {
        List<String> groups = dao.selectEmpGroupName();
        assertTrue(groups.size() > 0);
    }
    
    @Test
    public void crudAlarmRuleTest() {
        final String applicationId = "test_app_id";
        List<Rule> rules = new LinkedList<Rule>();
        
        for(int i = 0; i < 3; i++) {
            rules.add(new Rule(applicationId, CheckerCategory.SLOW_COUNT.getName(), 100, "test_group", true, true, ""));
        }
        
        dao.insertAppRule(rules);
        rules = dao.selectAppRule(applicationId);
        assertEquals(3, rules.size());
        
        for (Rule rule : rules) {
            assertEquals(applicationId, rule.getApplicationId());
        }
        
        dao.deleteAppRule(rules.get(0).getApplicationId());
        rules = dao.selectAppRule(applicationId);
        assertEquals(0, rules.size());
        
    }
    
    @Test
    public void crudAlarmGroupMemberTest() {
        List<AlarmEmp> emps = new LinkedList<AlarmEmp>();
        final String groupName = "s_mem_test";
        final String empName = "empName";
        
        for (int i = 0; i < 3; i++) {
            emps.add(new AlarmEmp(groupName, empName + i));    
        }

        dao.insertEmpGroupMember(emps);
        emps = dao.selectEmpGroupMember(groupName);
        assertEquals(3, emps.size());
        
        for (AlarmEmp emp : emps) {
            assertEquals(groupName, emp.getGroupName());
        }

        dao.deleteEmpGroupMember(emps.get(0).getGroupName());
        emps = dao.selectEmpGroupMember(groupName);
        assertEquals(0, emps.size());
    }

}