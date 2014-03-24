package com.nhn.pinpoint.testweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.util.Description;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class DoNothingController {

	@Description("아무일도 하지 않음.")
	@RequestMapping(value = "/donothing")
	public String donothing(Model model) {
		return "donothing";
	}
}
