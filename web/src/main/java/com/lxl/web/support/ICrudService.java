package com.lxl.web.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxl.common.entity.BaseEntity;
import com.lxl.common.vo.ResponseInfo;

import java.util.List;

/**
 * 基础增删改查
 *
 * @param <T>
 * @param <ID>
 */
public interface ICrudService<T extends BaseEntity, ID> extends IService<T> {

    /**
     * 插入对象
     *
     * @param m
     * @return
     */
    ResponseInfo<T> create(T m);

    /**
     * 编辑对象
     *
     * @param m
     * @param updateAllColumn
     * @return
     */
    ResponseInfo<T> update(T m, boolean updateAllColumn);

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    ResponseInfo<T> delById(ID id);

    /**
     * 通过id集合删除
     *
     * @param ids
     * @return
     */
    ResponseInfo<T> delete(String ids);

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    T findById(ID id);

    /**
     * 分页查询
     *
     * @param m
     * @param page
     * @return
     */
    ResponseInfo<T> findListByPage(T m, Page<T> page);

    /**
     * 分页查询指定字段
     *
     * @param m
     * @param page
     * @param properties
     * @return
     */
    ResponseInfo<T> findListByPage(T m, Page<T> page, String[] properties);

    /**
     * 查询集合
     *
     * @param m
     * @param properties
     * @return
     */
    ResponseInfo<T> list(T m, String[] properties);

    /**
     * 查询集合指定字段
     *
     * @param m
     * @param properties
     * @return
     */
    List<T> selectList(T m, String[] properties);

}
