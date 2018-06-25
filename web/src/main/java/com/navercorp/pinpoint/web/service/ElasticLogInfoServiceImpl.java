package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.ElasticLogDao;
import com.navercorp.pinpoint.web.dao.elasticsearch.ElasticQueryLogInfoDao;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.DataWithPaging;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.SearchRequestParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
@Service
public class ElasticLogInfoServiceImpl implements ElasticLogInfoService {

    @Autowired
    private ElasticLogDao elasticLogDao;

    @Override
    public String search(SearchRequestParams params) {
        DataWithPaging<Map> result = elasticLogDao.scrollSearch(params);
        List<Map> list = result.getList();

        if (list.isEmpty() || list.size() == 0){
            return "List Empty Data .......";
        }

        StringBuilder sb = new StringBuilder();
        list.stream().forEach(item -> {
            sb.append("<h5>");
            sb.append(item.get("message"));
            sb.append("</h5><hr/>");
        });
        return sb.toString();
    }
}
