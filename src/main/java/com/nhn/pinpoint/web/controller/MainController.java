package com.nhn.pinpoint.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nhn.pinpoint.web.service.CommonService;
import com.nhn.pinpoint.web.vo.Application;

/**
 * @author emeroad
 * @author netspider
 */
@Controller
public class MainController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CommonService commonService;

	@RequestMapping(value = "/applications", method = RequestMethod.GET)
	public String flow(Model model) {
		List<Application> applications = commonService.selectAllApplicationNames();
		model.addAttribute("applications", applications);

		logger.debug("/applications, {}", applications);

		return "applications";
	}

	@RequestMapping(value = "/serverTime", method = RequestMethod.GET)
	public String getServerTime(Model model) {
		model.addAttribute("currentServerTime", System.currentTimeMillis());
		return "serverTime";
	}
}