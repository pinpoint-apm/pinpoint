package com.navercorp.pinpoint.testapp.controller;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author koo.taejin
 */
@Controller(value = "apisController")
public class ApisController {

    private final RequestMappingHandlerMapping handlerMapping;
    
    private final Map<String, SortedSet<String>> apiMappings = new TreeMap<String, SortedSet<String>>(String.CASE_INSENSITIVE_ORDER);
    
    @Autowired
    public ApisController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }
    
    @PostConstruct
    private void initApiMappings() {
        Map<RequestMappingInfo, HandlerMethod> requestMappedHandlers = this.handlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> requestMappedHandlerEntry : requestMappedHandlers.entrySet()) {
            RequestMappingInfo requestMappingInfo = requestMappedHandlerEntry.getKey();
            HandlerMethod handlerMethod = requestMappedHandlerEntry.getValue();
            
            Class<?> handlerMethodBeanClazz = handlerMethod.getBeanType();
            if (handlerMethodBeanClazz == this.getClass()) {
                continue;
            }
            
            String controllerName = handlerMethodBeanClazz.getSimpleName();
            Set<String> mappedRequests = requestMappingInfo.getPatternsCondition().getPatterns();
            
            SortedSet<String> alreadyMappedRequests = this.apiMappings.get(controllerName);
            if (alreadyMappedRequests == null) {
                alreadyMappedRequests = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                this.apiMappings.put(controllerName, alreadyMappedRequests);
            }
            alreadyMappedRequests.addAll(mappedRequests);
        }
    }
    
    @RequestMapping(value = {"/index.html", "/apis"}, method = RequestMethod.GET)
    public String apis(Model model) {
        model.addAttribute("apiMappings", this.apiMappings);
        return "apis";
    }
    
}
