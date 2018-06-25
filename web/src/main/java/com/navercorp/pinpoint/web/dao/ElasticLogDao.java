package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.dao.elasticsearch.domain.DataWithPaging;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.SearchRequestParams;

import java.util.Map;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
public interface ElasticLogDao {

    DataWithPaging<Map> scrollSearch(SearchRequestParams searchRequestParams);

}
