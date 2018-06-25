package com.navercorp.pinpoint.web.dao.elasticsearch.domain;

import java.util.List;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
public class DataWithPaging<T> {
    private static final long serialVersionUID = -6195197401344995532L;
    private List<T> list;
    private Integer page;
    private Integer pageSize;
    private Integer total;
    private Integer totalPage;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
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

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }
}