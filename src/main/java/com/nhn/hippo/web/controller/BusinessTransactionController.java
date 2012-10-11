package com.nhn.hippo.web.controller;

import com.nhn.hippo.web.service.SpanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 *
 */
@Controller
public class BusinessTransactionController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SpanService spanService;

    @RequestMapping(value = "/selectTransaction", method = RequestMethod.GET)
    public ModelAndView flow(@RequestParam(value = "traceId", required = false) String traceId) {
        logger.debug("traceId:{}", traceId);
        List<SpanAlign> spanAligns = spanService.selectSpan(traceId);
        ModelAndView mv = new ModelAndView("selectTransaction");
        mv.addObject("spanList", spanAligns);
        mv.addObject("traceId", traceId);
        return mv;
    }
}
