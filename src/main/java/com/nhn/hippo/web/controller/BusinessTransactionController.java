package com.nhn.hippo.web.controller;

import com.nhn.hippo.web.service.SpanAlign;
import com.nhn.hippo.web.service.SpanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 *
 */
@Controller
public class BusinessTransactionController {
    @Autowired
    private SpanService spanService;

    @RequestMapping(value = "/selectTransaction")
    public ModelAndView flow(@RequestParam("uuid") String uuid) {
        List<SpanAlign> spanAligns = spanService.selectSpan(uuid);

        return new ModelAndView("selectTransaction", "span", spanAligns);
    }
}
