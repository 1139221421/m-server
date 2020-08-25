package com.lxl.web.support;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxl.common.entity.BaseEntity;
import com.lxl.common.vo.RequestInfo;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.utils.common.IdUtil;
import com.lxl.web.annotations.CrudConfig;
import com.lxl.web.annotations.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 基础增删改查控制器
 *
 * @author：
 */
@Slf4j
public abstract class BaseCrudController<T extends BaseEntity, S extends ICrudService<T, ID>, ID> extends BaseController {

    /**
     * 初始化Controller
     */
    @PostConstruct
    private void initCrudController() {
        CrudConfig crudConfig = getCrudConfig();
        /**
         * 增删改查权限名自定义
         */
        if (crudConfig != null) {
            Method[] methods = getClass().getSuperclass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                switch (methods[i].getName()) {
                    case "create":
                        if (!"create".equals(crudConfig.createPermission())) {
                            setAnnotationValue(methods[i].getAnnotation(Permission.class), "value", crudConfig.createPermission());
                        }
                        break;
                    case "list":
                        if (!"list".equals(crudConfig.retrievePermission())) {
                            setAnnotationValue(methods[i].getAnnotation(Permission.class), "value", crudConfig.retrievePermission());
                        }
                        break;
                    case "update":
                        if (!"update".equals(crudConfig.updatePermission())) {
                            setAnnotationValue(methods[i].getAnnotation(Permission.class), "value", crudConfig.updatePermission());
                        }
                        break;
                    case "delete":
                        if (!"delete".equals(crudConfig.deletePermission())) {
                            setAnnotationValue(methods[i].getAnnotation(Permission.class), "value", crudConfig.deletePermission());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }


    /**
     * 查询
     *
     * @param requestInfo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/list")
    @Permission(value = "list")
    public ResponseInfo<T> list(@RequestBody RequestInfo<T> requestInfo) {
        ResponseInfo<T> beforeResult = listBefore(requestInfo.getM());
        if (beforeResult != null) {
            return beforeResult;
        }
        // 查询字段
        String[] properties = ((requestInfo.getSimple() != null && requestInfo.getSimple()) ? getSimpleProperties() : getProperties());
        Page<T> pageM = new Page<>();
        if (requestInfo.getPageSize() == null || requestInfo.getPage() == null) {
            requestInfo.setPageSize(1000);
            requestInfo.setPage(0);
        }
        // 排序相关
        if (requestInfo.getSortOrder() != null) {
            if (RequestInfo.SortOrder.ASC.equals(requestInfo.getSortOrder())) {
                pageM.addOrder(OrderItem.ascs(requestInfo.getSortField()));
            } else {
                pageM.addOrder(OrderItem.descs(requestInfo.getSortField()));
            }
        } else {
            if (isAsc()) {
                pageM.addOrder(OrderItem.asc(getSortField()));
            } else {

                pageM.addOrder(OrderItem.desc(getSortField()));
            }
        }
        pageM.setCurrent(requestInfo.getPage());
        pageM.setSize(requestInfo.getPageSize());
        return getService().findListByPage(requestInfo.getM(), pageM);
    }

    /**
     * 增加
     *
     * @param m
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Permission(value = "create")
    public ResponseInfo<T> create(@RequestBody T m) {
        ResponseInfo<T> beforeResult = createBefore(m);
        if (beforeResult != null) {
            return beforeResult;
        }
        ResponseInfo<T> result = getService().create(m);
        createAfter(m, result);
        return result;
    }

    /**
     * 更新
     *
     * @param m
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Permission(value = "update")
    public ResponseInfo<T> update(@RequestBody T m) {
        ResponseInfo<T> beforeResult = updateBefore(m);
        if (beforeResult != null) {
            return beforeResult;
        }
        ResponseInfo<T> result = getService().update(m, isUpdateAllColumn());
        updateAfter(m, result);
        return result;
    }


    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Permission(value = "delete")
    public ResponseInfo<T> delete(@RequestBody String ids) {
        String[] arr = ids.split(",");
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(IdUtil.unwrap(Long.valueOf(s)));
            sb.append(",");
        }
        ids = sb.toString().substring(0, sb.length() - 1);
        ResponseInfo<T> beforeResult = deleteBefore(ids);
        if (beforeResult != null) {
            return beforeResult;
        }
        ResponseInfo<T> result = getService().delete(ids);
        deleteAfter(ids, result);
        return result;
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @Permission(value = "delete")
    public ResponseInfo<T> deleteByPrimary(ID id) {
        return getService().delById(id);
    }

    /**
     * 获得服务层 供子类覆盖
     *
     * @return
     */
    protected abstract S getService();

    private String[] getProperties() {
        CrudConfig crudConfig = getCrudConfig();
        if (crudConfig != null && crudConfig.properties().length > 0) {
            return crudConfig.properties();
        } else {
            return null;
        }
    }

    private String[] getSimpleProperties() {
        CrudConfig crudConfig = getCrudConfig();
        if (crudConfig != null && crudConfig.simpleProperties().length > 0) {
            return crudConfig.simpleProperties();
        } else {
            return null;
        }
    }

    protected ResponseInfo<T> createBefore(T m) {
        if (m != null) {
            m.setId(IdUtil.unwrap(m.getId()));
        }
        return null;
    }

    protected ResponseInfo<T> deleteBefore(String ids) {
        return null;
    }

    protected ResponseInfo<T> updateBefore(T m) {
        if (m != null) {
            m.setId(IdUtil.unwrap(m.getId()));
        }
        return null;
    }

    protected ResponseInfo<T> listBefore(T m) {
        if (m != null) {
            m.setId(IdUtil.unwrap(m.getId()));
            m.setCreatorId(IdUtil.unwrap(m.getCreatorId()));
        }
        return null;
    }

    protected void createAfter(T m, ResponseInfo<T> result) {
    }

    protected void deleteAfter(String ids, ResponseInfo<T> result) {
    }

    protected void updateAfter(T m, ResponseInfo<T> result) {
    }

    protected void listAfter(T m, ResponseInfo<T> result) {
    }

    /**
     * 从注解配置获得是否更新所有字段
     *
     * @return
     */
    private boolean isUpdateAllColumn() {
        CrudConfig crudConfig = getCrudConfig();
        if (crudConfig != null && crudConfig.updateAllColumn()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 默认排序字段
     *
     * @return
     */
    private String getSortField() {
        CrudConfig crudConfig = getCrudConfig();
        if (crudConfig != null) {
            return crudConfig.sortField();
        }
        return null;
    }

    /**
     * 默认是否升序
     *
     * @return
     */
    private boolean isAsc() {
        CrudConfig crudConfig = getCrudConfig();
        if (crudConfig != null && crudConfig.isAsc()) {
            return true;
        }
        return false;
    }

    private CrudConfig getCrudConfig() {
        return getClass().getAnnotation(CrudConfig.class);
    }

    private void setAnnotationValue(Annotation annotationClass, String key, Object value) {
        if (annotationClass == null) {
            return;
        }
        try {
            //获取这个代理实例所持有的 InvocationHandler
            InvocationHandler h = Proxy.getInvocationHandler(annotationClass);
            // 获取 AnnotationInvocationHandler 的 memberValues 字段
            Field hField = null;
            hField = h.getClass().getDeclaredField("memberValues");
            // 因为这个字段事 private final 修饰，所以要打开权限
            hField.setAccessible(true);
            Map<String, Object> memberValues = null;
            memberValues = (Map<String, Object>) hField.get(h);
            // 修改 权限注解value 属性值
            memberValues.put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
