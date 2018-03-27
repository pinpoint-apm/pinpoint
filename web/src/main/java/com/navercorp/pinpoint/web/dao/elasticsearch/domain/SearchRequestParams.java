package com.navercorp.pinpoint.web.dao.elasticsearch.domain;

import java.util.HashMap;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
public class SearchRequestParams {

    private String index;
    private HashMap<String, Object> paramsMap;

    private Integer page;
    private Integer pageSize;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
    }

    public HashMap<String, Object> getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(HashMap<String, Object> paramsMap) {
        this.paramsMap = paramsMap;
    }
}
