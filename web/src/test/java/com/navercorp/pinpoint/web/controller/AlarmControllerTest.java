package com.nhn.pinpoint.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.vo.AlarmEmp;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.controller.AlarmController.EmpGroup;
import com.nhn.pinpoint.web.controller.AlarmController.RuleGroup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:servlet-context.xml", "classpath:applicationContext-test.xml"})
public class AlarmControllerTest {

    @Autowired
    AlarmController controller;
    
    @Test
    public void alarmGroupListTest() {
        List<String> groups = controller.alarmGroupList();
        assertTrue(groups.size() > 0);
    }
    
    @Test
    public void insertAlarmEmpTest() {
        final String groupName = "con_group";
        List<AlarmEmp> emps = new LinkedList<AlarmEmp>();
        
        for (int i = 0; i < 3; i++) {
            emps.add(new AlarmEmp(groupName, "con_emp" + i));
        }
        
        EmpGroup empgroup = new EmpGroup();
        empgroup.setEmps(emps);
        controller.insertMember(empgroup);
        ModelAndView mv = controller.getMember(groupName);
        emps = (List<AlarmEmp>) mv.getModel().get("groupMember");
        assertEquals(3, emps.size());
        
        empgroup.setEmps(emps);
        controller.deleteMember(empgroup);
        mv = controller.getMember(groupName);
        emps = (List<AlarmEmp>) mv.getModel().get("groupMember");
        assertEquals(0, emps.size());
    }
    
    @Test
    public void updateAlarmEmpTest() {
        final String groupName = "con_group";
        List<AlarmEmp> emps = new LinkedList<AlarmEmp>();
        
        for (int i = 0; i < 3; i++) {
            emps.add(new AlarmEmp(groupName, "con_emp" + i));
        }
        
        EmpGroup empgroup = new EmpGroup();
        empgroup.setEmps(emps);
        controller.insertMember(empgroup);
        ModelAndView mv = controller.getMember(groupName);
        emps = (List<AlarmEmp>) mv.getModel().get("groupMember");
        assertEquals(3, emps.size());

        List<AlarmEmp> updateEmps = new LinkedList<AlarmEmp>();
        for (int i = 0; i < 3; i++) {
            AlarmEmp emp = new AlarmEmp(groupName, "con_emp_" + i);
            emp.setId(emps.get(i).getId());
            updateEmps.add(emp);
        }
        
        updateEmps.add(new AlarmEmp(groupName, "con_emp_" + 4));
        empgroup.setEmps(updateEmps);
        controller.updateMember(empgroup);
        mv = controller.getMember(groupName);
        updateEmps = (List<AlarmEmp>) mv.getModel().get("groupMember");
        assertEquals(4, updateEmps.size());
        
        empgroup.setEmps(updateEmps);
        controller.deleteMember(empgroup);
        mv = controller.getMember(groupName);
        emps = (List<AlarmEmp>) mv.getModel().get("groupMember");
        assertEquals(0, emps.size());
    }
    
    @Test
    public void insertRuleTest() {
        final String ApplicationName = "test_app_name";
        List<Rule> rules = new LinkedList<Rule>();
        
        for (int i = 0; i < 3; i++) {
            rules.add(new Rule(ApplicationName, CheckerCategory.SLOW_COUNT.getName(), 100, "test_group", true, true));
        }
        
        RuleGroup ruleGroup = new RuleGroup();
        ruleGroup.setRuleList(rules);
        controller.insertRule(ruleGroup);
        ModelAndView mv = controller.getRule(ApplicationName);
        rules = (List<Rule>) mv.getModel().get("ruleList");
        assertEquals(3, rules.size());
        
        ruleGroup.setRuleList(rules);
        controller.deleteRule(ruleGroup);
        mv = controller.getRule(ApplicationName);
        rules = (List<Rule>) mv.getModel().get("ruleList");
        assertEquals(0, rules.size());
    }
    
    @Test
    public void updateRuleTest() {
        final String applicationName = "test_app_name";
        List<Rule> rules = new LinkedList<Rule>();
        
        for (int i = 0; i < 3; i++) {
            rules.add(new Rule(applicationName, CheckerCategory.SLOW_COUNT.getName(), 100, "test_group", true, true));
        }
        
        RuleGroup ruleGroup = new RuleGroup();
        ruleGroup.setRuleList(rules);
        controller.insertRule(ruleGroup);
        ModelAndView mv = controller.getRule(applicationName);
        rules = (List<Rule>) mv.getModel().get("ruleList");
        assertEquals(3, rules.size());

        List<Rule> updateRules = new LinkedList<Rule>();
        for (int i = 0; i < 3; i++) {
            Rule rule = new Rule(applicationName, CheckerCategory.SLOW_COUNT.getName(), 1000, "test_group", true, true);
            rule.setId(rules.get(i).getId());
            updateRules.add(rule);
        }
        
        updateRules.add(new Rule(applicationName, CheckerCategory.SLOW_COUNT.getName(), 1000, "test_group", true, true));
        
        ruleGroup.setRuleList(updateRules);
        controller.updateRule(ruleGroup);
        mv = controller.getRule(applicationName);
        updateRules = (List<Rule>) mv.getModel().get("ruleList"); 
        assertEquals(4, updateRules.size());
        
        ruleGroup.setRuleList(updateRules);
        controller.deleteRule(ruleGroup);
        mv = controller.getRule(applicationName);
        rules = (List<Rule>) mv.getModel().get("ruleList");
        assertEquals(0, rules.size());
    }
    
    @Test
    public void getAlarmRuleNamesTest() {
        List<String> names = controller.getAlarmRuleNames();
        assertEquals(CheckerCategory.getNames().size(), names.size());
    }

}
