package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.dao.elasticsearch.domain.DataWithPaging;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.SearchRequestParams;
import com.navercorp.pinpoint.web.service.ElasticLogInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

/**
 * Created by yuanxiaozhong on 2018/3/27.
 */
@Controller
public class BusinessLogInfoController {

    @Autowired
    private ElasticLogInfoService elasticLogInfoService;

    @RequestMapping(value = "/logInfo", method = RequestMethod.GET, produces = "text/html;charset=utf-8")
    @ResponseBody
    public String logInfo(@RequestParam(value= "agentId",required=true) String agentId,
                          @RequestParam (value= "transactionId", required=true) String transactionId,
                          @RequestParam(value= "spanId" , required=false) String spanId,
                          @RequestParam(value="time" , required=true) long time ,
                          @RequestParam(value = "logEsIndexPrefix", required = true) String prefix,
                          @RequestParam(value = "page", required = false) Integer page,
                          @RequestParam(value = "pageSize", required = false) Integer pageSize){

        SearchRequestParams params = new SearchRequestParams();
        params.setIndex(prefix + "*");
        params.setPage((page == null || page <= 0) ? 1 : page);
        params.setPageSize((pageSize == null || pageSize <= 0) ? 10 : pageSize);

        HashMap<String, Object> map = new HashMap<>();
        map.put("txId", transactionId);
        if (spanId != null && !"".equals(spanId)){
            map.put("spanId", spanId);
        }
        params.setParamsMap(map);
        return elasticLogInfoService.search(params);
    }

}
