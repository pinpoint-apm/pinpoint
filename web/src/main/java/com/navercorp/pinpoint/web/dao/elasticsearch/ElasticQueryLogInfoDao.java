package com.navercorp.pinpoint.web.dao.elasticsearch;

import com.navercorp.pinpoint.web.dao.ElasticLogDao;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.DataWithPaging;
import com.navercorp.pinpoint.web.dao.elasticsearch.domain.SearchRequestParams;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
@Repository
public class ElasticQueryLogInfoDao implements ElasticLogDao{

    @Autowired
    private ElasticTemplate elasticTemplate;


    @Override
    public DataWithPaging<Object> scrollSearch(SearchRequestParams searchRequestParams) {
        Client client = elasticTemplate.getClient();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        fillFilterParams(boolQueryBuilder, searchRequestParams.getParamsMap());
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(searchRequestParams.getIndex());

        searchRequestBuilder.setQuery(boolQueryBuilder)
                .setFrom((searchRequestParams.getPage() - 1) * searchRequestParams.getPageSize())
                .setSize(searchRequestParams.getPageSize());
        SearchResponse sr = searchRequestBuilder.execute().actionGet();

        //获取所有的values
        DataWithPaging<Object> dataSet = new DataWithPaging<>();
        dataSet.setList(processResultHits(sr.getHits().getHits()));
        dataSet.setPageSize(searchRequestParams.getPageSize());
        dataSet.setPage(searchRequestParams.getPage());
        dataSet.setTotal((int) sr.getHits().getTotalHits());
        int pageCount = (int) sr.getHits().getTotalHits() / searchRequestParams.getPageSize();
        dataSet.setTotalPage(sr.getHits().getTotalHits() % searchRequestParams.getPageSize() == 0 ? pageCount : pageCount + 1);
        return dataSet;
    }

    private void fillFilterParams(final BoolQueryBuilder boolQueryBuilder, HashMap<String, Object> map){
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> entry = iterator.next();
            boolQueryBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
        }
    }

    private List<Object> processResultHits(SearchHit[] hits){
        List<Object> list = new ArrayList<>();
        Arrays.stream(hits).forEach(item -> processResultHitsItem(list, item));
        return list;
    }

    private void processResultHitsItem(List<Object> list, SearchHit item) {
        Map<String, Object> source = item.getSource();
        list.add(source);
    }
}
