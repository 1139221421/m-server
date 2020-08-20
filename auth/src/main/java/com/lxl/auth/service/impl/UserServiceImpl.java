package com.lxl.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lxl.auth.dao.UserMapper;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.UserService;
import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.auth.vo.LoginUserInfo;
import com.lxl.common.entity.auth.User;
import com.lxl.common.entity.message.Message;
import com.lxl.common.feign.message.MessageFeign;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.utils.common.PasswordUtil;
import com.lxl.web.elastic.ElasticCustomerOperate;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageFeign messageFeign;

    @Resource
    private ElasticCustomerOperate elasticCustomerOperate;

    @Resource
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public void crate(User user) {
        log.debug("添加用户：{}", JSON.toJSONString(user));
        userMapper.insert(user);
        user.setUsername(user.getUsername() + "-template");
        elasticCustomerOperate.save(user);
        user.setUsername(user.getUsername() + "-repository");
        userRepository.save(user);
    }

    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public LoginUserInfo veryfiyUser(LoginRequestInfo loginRequestInfo) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", loginRequestInfo.getUsername());
        queryWrapper.last("limit 1");
        User user = userMapper.selectOne(queryWrapper);
        if (user != null && PasswordUtil.getPwd(user.getSalt(), loginRequestInfo.getPassword()).equals(user.getPassword())) {
            return new LoginUserInfo<>(user);
        }
        return new LoginUserInfo<>(user, false);
    }

    /**
     * tcc业务
     *
     * @param user
     */
    @Override
    @GlobalTransactional
    public void tccUpdate(User user) {
        if (!this.updatePrepare(new BusinessActionContext(), user)) {
            throw new RuntimeException("TCC全局事务-用户更新失败");
        }
        Message message = new Message();
        message.setTitle("TCC全局事务-用户修改-用户信息：" + JSON.toJSONString(user));
        if (!messageFeign.savePrepare(message).getSuccess()) {
            throw new RuntimeException("TCC全局事务-消息保存失败");
        }
    }

    /**
     * 检查和资源预留(检查并做数据的更改)
     * Transactional 是为了本地事务一致，回滚是可以不用考虑本地事务
     *
     * @param actionContext
     * @param user
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean updatePrepare(BusinessActionContext actionContext, User user) {
        userMapper.updateById(user);
        // 其他数据库操作。。。
        return true;
    }

    /**
     * 提交事务 (验证数据是否更改)
     * 幂等：多次调用方法（Confirm）
     *
     * @param actionContext
     * @return
     */
    @Override
    public boolean updateCommit(BusinessActionContext actionContext) {
        // 验证user是否修改
        User user = (User) actionContext.getActionContext("user");
        User old = userMapper.selectById(user.getId());
        // 模拟只修改了name
        if (old != null && old.getUsername().equals(user.getUsername())) {
            return true;
        }
        // 此处也可以不返回false，可以再次执行Prepare业务并返回true，相当于Prepare的补偿，但是特殊情况要考虑幂等
        return false;
    }

    /**
     * 取消回滚
     * 空回滚：Try未执行，Cancel 执行了
     * 悬挂：Cancel接口 比 Try接口先执行
     *
     * @param actionContext
     * @return
     */
    @Override
    public boolean updateRollback(BusinessActionContext actionContext) {
        //回滚到事务提交前的状态
        return true;
    }

    /**
     * 扣减余额
     *
     * @param id
     * @param reduce
     * @return
     */
    @Override
    public ResponseInfo reduceAccountBalance(Long id, BigDecimal reduce) {
        userMapper.reduceAccountBalance(id, reduce);
        return ResponseInfo.createSuccess();
    }
}
