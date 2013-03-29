package com.nhn.hippo.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.vo.Application;

/**
 * 
 * @author netspider
 */
@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FlowChartService flow;

    @RequestMapping(value = "/applications", method = RequestMethod.GET)
    public String flow(Model model) {
        List<Application> applications = flow.selectAllApplicationNames();
        model.addAttribute("applications", applications);

        logger.debug("Applications, {}", applications);

        return "applications";
    }
}