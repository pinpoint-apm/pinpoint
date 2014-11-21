package com.nhn.pinpoint.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.web.service.AdminService;

/**
 * @author netspider
 */
@Controller
public class AdminController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AdminService adminService;

	@RequestMapping(value = "/admin/removeApplicationName", method = RequestMethod.GET)
	@ResponseBody
	public String removeApplicationName(@RequestParam("applicationName") String applicationName) {
		logger.info("remove application name. {}", applicationName);
		try {
			adminService.removeApplicationName(applicationName);
			return "OK";
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	@RequestMapping(value = "/admin/removeAgentId", method = RequestMethod.GET)
	@ResponseBody
	public String removeAgentId(
	        @RequestParam(value = "applicationName", required = true) String applicationName,
	        @RequestParam(value = "agentId", required = true) String agentId) {
	    logger.info("remove {}'s agentId [{}]", applicationName, agentId);
	    try {
	        adminService.removeAgentId(applicationName, agentId);
	        return "OK";
	    } catch (Exception e) {
	        return e.getMessage();
	    }
	}
}