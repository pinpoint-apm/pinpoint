package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.elasticsearch.ElasticQueryLogInfoDao;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.DataWithPaging;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.SearchRequestParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
@Service
public class ElasticLogInfoServiceImpl implements ElasticLogInfoService {

    @Autowired
    private ElasticQueryLogInfoDao elasticQueryLogInfoDao;

    @Override
    public DataWithPaging<Object> search(SearchRequestParams params) {
        return elasticQueryLogInfoDao.scrollSearch(params);
    }
}
