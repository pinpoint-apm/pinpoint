package com.nhn.pinpoint.web.controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.vo.AlarmEmp;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.dao.mysql.MySqlAlarmResourceDao;
import com.nhn.pinpoint.web.service.CommonService;
import com.nhn.pinpoint.web.vo.Application;

@Controller
public class AlarmController {

    @Autowired
    private MySqlAlarmResourceDao dao;
    
    @Autowired
    private CommonService commonService;
    
    @RequestMapping(value = "/alarmGroupList")
    public List<String> alarmGroupList() {
        return dao.selectEmpGroupName();
    }
    
    @RequestMapping(value = "/alarmGroup/getMember")
    public ModelAndView getMember(String groupName) {
        ModelAndView mv = new ModelAndView();
        
        if (groupName != null) {
            List<AlarmEmp> members = dao.selectEmpGroupMember(groupName);
            mv.addObject("groupMember", members);    
        }

        List<String> groupNameList = alarmGroupList();
        mv.addObject("groupNameList", groupNameList);
        
        mv.setViewName("alarm/empGroup");
        
        return mv;
    }
    
    public static class EmpGroup {
        private List<AlarmEmp> emps;

        public List<AlarmEmp> getEmps() {
            return emps;
        }

        public void setEmps(List<AlarmEmp> emps) {
            this.emps = emps;
        }
    }
    
    private List<AlarmEmp> removeEmptyEmp(List<AlarmEmp> emps) {
        List<AlarmEmp> newEmps = new LinkedList<AlarmEmp>();
        
        for(AlarmEmp emp : emps) {
            if (emp.getGroupName() != null && emp.getEmpId() != null) {
                if (!emp.getGroupName().isEmpty() && !emp.getEmpId().isEmpty()) {
                    newEmps.add(emp);
                }
            }
        }
        
        return newEmps;
    }
    
    @RequestMapping(value = "/alarmGroup/insertMember")
    public String insertMember(EmpGroup empGroup) {
        List<AlarmEmp> emps = removeEmptyEmp(empGroup.getEmps()); 
        dao.insertEmpGroupMember(emps);
        return "redirect:/alarmGroup/getMember.pinpoint?groupName=" + emps.get(0).getGroupName();
    }
    
    @RequestMapping(value = "/alarmGroup/deleteMember")
    public String deleteMember(EmpGroup empGroup) {
        dao.deleteEmpGroupMember(empGroup.getEmps().get(0).getGroupName());
        return "redirect:/alarmGroup/getMember.pinpoint?groupName=" + empGroup.getEmps().get(0).getGroupName();
    }
    
    @RequestMapping(value = "/alarmGroup/updateMember")
    public String updateMember(EmpGroup empGroup) {
        List<AlarmEmp> emps = removeEmptyEmp(empGroup.getEmps()); 
        dao.deleteEmpGroupMember(emps.get(0).getGroupName());
        dao.insertEmpGroupMember(emps);
        return "redirect:/alarmGroup/getMember.pinpoint?groupName=" + emps.get(0).getGroupName();
    }
    
    @RequestMapping(value = "/alarmRule/ruleNameList")
    public List<String> getAlarmRuleNames() {
        return CheckerCategory.getNames();
    }
    
    @RequestMapping(value = "/alarmRule/getRule")
    public ModelAndView getRule(String applicationName) {
        ModelAndView mv = new ModelAndView();
        
        if (applicationName != null) {
            List<Rule> ruleList = dao.selectAppRule(applicationName);
            mv.addObject("ruleList", ruleList);    
        }

        List<Application> applicationList = commonService.selectAllApplicationNames();
        List<String> applicationNameList = new LinkedList<String>(); 
        
        for(Application application : applicationList) {
            applicationNameList.add(application.getName());
        }
        
        mv.addObject("applicationNameList", applicationNameList);
        mv.addObject("empGroupNameList", alarmGroupList());
        mv.addObject("checkerNameList", getAlarmRuleNames());
        mv.setViewName("alarm/rule");
        return mv;
    }
    
    public static class RuleGroup {
        private List<Rule> ruleList;

        public List<Rule> getRuleList() {
            return ruleList;
        }

        public void setRuleList(List<Rule> ruleList) {
            this.ruleList = ruleList;
        }
    }
    
    private List<Rule> removeEmptyRule(List<Rule> ruleList) {
        List<Rule> newRuleList = new LinkedList<Rule>();
        
        for(Rule rule : ruleList) {
            if (rule.getApplicationId() != null && !rule.getApplicationId().isEmpty()) {
                if (rule.getThreshold() >= 0) {
                    newRuleList.add(rule);
                }
            }
        }
        
        return newRuleList;
    }
    
    @RequestMapping(value = "/alarmRule/insertRule")
    public String insertRule(RuleGroup ruleGroup) {
        List<Rule> ruleList = removeEmptyRule(ruleGroup.getRuleList());
        dao.insertAppRule(ruleList);
        return "redirect:/alarmRule/getRule.pinpoint?applicationName=" + ruleList.get(0).getApplicationId();
    }
    
    @RequestMapping(value = "/alarmRule/deleteRule")
    public String deleteRule(RuleGroup ruleGroup) {
        dao.deleteAppRule(ruleGroup.getRuleList().get(0).getApplicationId());
        return "redirect:/alarmRule/getRule.pinpoint?applicationName=" + ruleGroup.getRuleList().get(0).getApplicationId();
    }
    
    @RequestMapping(value = "/alarmRule/updateRule")
    public String updateRule(RuleGroup ruleGroup) {
        List<Rule> ruleList = removeEmptyRule(ruleGroup.getRuleList()); 
        dao.deleteAppRule(ruleList.get(0).getApplicationId());
        dao.insertAppRule(ruleList);
        return "redirect:/alarmRule/getRule.pinpoint?applicationName=" + ruleGroup.getRuleList().get(0).getApplicationId();
    }
    
}
