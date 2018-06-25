package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.elasticsearch.domain.DataWithPaging;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.SearchRequestParams;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
public interface ElasticLogInfoService {

    /**
     *
     * @param params
     * @return
     */
    String search(SearchRequestParams params);
}
