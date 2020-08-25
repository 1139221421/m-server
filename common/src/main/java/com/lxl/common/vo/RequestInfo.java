package com.lxl.common.vo;

import com.alibaba.fastjson.JSONObject;
import com.lxl.common.entity.BaseEntity;

import java.io.Serializable;

/**
 * 请求体
 *
 * @Author
 */

public class RequestInfo<T extends BaseEntity> implements Serializable {
    private static final long serialVersionUID = 23177911113046671L;
    private T m;
    private Integer pageSize;
    private Integer page;
    private Boolean simple;
    private String sortField;
    private String sortOrder;
    private JSONObject mapData;

    public interface SortOrder {
        String ASC = "asc";
        String DESC = "desc";
    }

    public JSONObject getMapData() {
        return mapData;
    }

    public void setMapData(JSONObject mapData) {
        this.mapData = mapData;
    }

    public T getM() {
        return m;
    }

    public void setM(T m) {
        this.m = m;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Boolean getSimple() {
        return simple;
    }

    public void setSimple(Boolean simple) {
        this.simple = simple;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
