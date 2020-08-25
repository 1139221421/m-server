package com.lxl.web.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxl.common.entity.BaseEntity;
import com.lxl.common.enums.LogTypeEnum;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.utils.common.IdUtil;
import com.lxl.web.annotations.Log;
import com.lxl.web.redis.RedisCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础增删改查
 *
 * @param <M>
 * @param <T>
 * @param <ID>
 */
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CrudServiceImpl<M extends BaseMapper<T>, T extends BaseEntity, ID extends Serializable> extends BaseService<M, T> implements ICrudService<T, ID> {

    @Resource
    protected RedisCacheUtils redisCacheUtils;

    @Resource
    protected OperatorUtils operatorUtils;

    /**
     * 插入对象
     *
     * @param m
     * @return
     */
    @Override
    @Log(LogTypeEnum.CREATE)
    public ResponseInfo<T> create(T m) {
        ResponseInfo<T> result = new ResponseInfo<>(false);
        ResponseInfo<T> beforeResult = createBefore(m);
        if (beforeResult != null) {
            return beforeResult;
        }
        if (save(m)) {
            createAfter(m);
            result.setBusinessData(m);
            result.setSuccess(true);
        } else {
            result.setMessage("添加失败");
            return result;
        }
        return result;
    }

    /**
     * 编辑对象
     *
     * @param m
     * @param updateAllColumn
     * @return
     */
    @Override
    @Log(LogTypeEnum.UPDATE)
    public ResponseInfo<T> update(T m, boolean updateAllColumn) {
        ResponseInfo<T> result = new ResponseInfo<>(false);
        if (m == null || m.getId() == null) {
            result.setMessage("请指定要修改记录");
            return result;
        }
        m.setId(IdUtil.unwrap(m.getId()));
        T oldM = getById(m.getId());
        if (oldM == null) {
            result.setMessage("未找到修改记录");
            return result;
        }
        ResponseInfo<T> beforeResult = updateBefore(m, oldM);
        if (beforeResult != null) {
            return beforeResult;
        }
        boolean status = false;
        if (updateAllColumn) {
            // 更新所有字段
            BeanUtils.copyProperties(oldM, m);
            status = updateById(m);
        } else {
            status = updateById(m);
            BeanUtils.copyProperties(oldM, m);
        }
        if (status) {
            updateAfter(m, oldM);
            result.setBusinessData(m);
            result.setSuccess(true);
            return result;
        } else {
            result.setMessage("更新失败");
            return result;
        }
    }

    /**
     * 通过id集合删除
     *
     * @param ids
     * @return
     */
    @Override
    @Log(LogTypeEnum.DELETE)
    public ResponseInfo delete(String ids) {
        ResponseInfo result = new ResponseInfo<>(false);
        String[] idArray = ids.split(",");
        if (idArray.length == 0) {
            result.setMessage("请选择要删除的记录");
        } else {
            List<Long> idList = new ArrayList<>();
            for (String s : idArray) {
                idList.add(IdUtil.unwrap(Long.valueOf(s)));
            }
            QueryWrapper<T> ew = new QueryWrapper<>();
            ResponseInfo beforeResult = deleteBefore(idList, ew);
            if (beforeResult != null) {
                return beforeResult;
            }
            ew.in("id", idList);
            if (remove(ew)) {
                deleteAfter(idList);
                result.setSuccess(true);
            } else {
                result.setMessage("删除失败");
            }
        }
        return result;
    }

    @Override
    @Log(LogTypeEnum.DELETE)
    public ResponseInfo delById(ID id) {
        id = (ID) IdUtil.unwrap((Long) id);
        ResponseInfo result = new ResponseInfo<>(false);
        if (id == null) {
            result.setMessage("请选择要删除的记录");
            return result;
        }
        ResponseInfo beforeResult = deleteBefore(id);
        if (beforeResult != null) {
            return beforeResult;
        }
        if (removeById(id)) {
            deleteAfter(id);
            result.setSuccess(true);
        } else {
            result.setMessage("删除失败");
        }
        return result;
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @Override
    public T findById(ID id) {
        return getById(IdUtil.unwrap((Long) id));
    }

    /**
     * 分页查询
     *
     * @param m
     * @param page
     * @return
     */
    @Override
    public ResponseInfo<T> findListByPage(T m, Page<T> page) {
        return findListByPage(m, page, null);
    }

    /**
     * 分页查询
     *
     * @param m
     * @param page
     * @param properties
     * @return
     */
    @Override
    public ResponseInfo<T> findListByPage(T m, Page<T> page, String[] properties) {
        ResponseInfo<T> result = new ResponseInfo<>(false);
        QueryWrapper<T> ew = new QueryWrapper<>(m);
        if (properties != null) {
            ew.select(properties);
        }
        ResponseInfo<T> beforeResult = listBefore(m, ew);
        if (beforeResult != null) {
            return beforeResult;
        }
        IPage<T> page1 = page(page, ew);
        listAfter(m, page1.getRecords());
        result.setPage(page1.getRecords(), page1.getTotal());
        result.setSuccess(true);
        return result;
    }

    /**
     * 查询列表
     *
     * @param m
     * @param properties
     * @return
     */
    @Override
    public ResponseInfo<T> list(T m, String[] properties) {
        ResponseInfo<T> result = new ResponseInfo<>(false);
        result.setPage(selectList(m, properties));
        result.setSuccess(true);
        return result;
    }

    /**
     * 查询集合
     *
     * @param m
     * @param properties
     * @return
     */
    @Override
    public List<T> selectList(T m, String[] properties) {
        QueryWrapper<T> ew = new QueryWrapper<>(m);
        if (properties != null) {
            ew.select(properties);
        }
        return list(ew);
    }

    protected ResponseInfo<T> createBefore(T m) {
        return null;
    }

    protected ResponseInfo<T> updateBefore(T m, T oldM) {
        return null;
    }

    protected ResponseInfo<T> listBefore(T m, QueryWrapper<T> ew) {
        return null;
    }

    protected ResponseInfo deleteBefore(ID id) {
        return null;
    }

    protected ResponseInfo deleteBefore(List<Long> ids, QueryWrapper<T> ew) {
        return null;
    }

    protected void createAfter(T m) {
    }

    protected void updateAfter(T m, T oldM) {
    }

    protected void listAfter(T m, List<T> resultList) {
    }

    protected void deleteAfter(ID id) {
    }

    protected void deleteAfter(List<Long> ids) {
    }

}
