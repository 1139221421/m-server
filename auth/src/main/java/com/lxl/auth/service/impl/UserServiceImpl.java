package com.lxl.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxl.auth.dao.UserMapper;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.IUserService;
import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.auth.vo.LoginUserInfo;
import com.lxl.common.entity.auth.User;
import com.lxl.common.entity.message.Message;
import com.lxl.common.feign.message.MessageFeign;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.utils.common.PasswordUtil;
import com.lxl.web.elastic.ElasticCustomerOperate;
import com.lxl.web.support.CrudServiceImpl;
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
public class UserServiceImpl extends CrudServiceImpl<UserMapper, User, Long> implements IUserService {

    @Resource
    private ElasticCustomerOperate elasticCustomerOperate;

    @Resource
    private UserRepository userRepository;

    @Override
    protected ResponseInfo<User> createBefore(User m) {
        log.debug("添加用户：{}", JSON.toJSONString(m));
        return null;
    }

    @Override
    protected void createAfter(User m) {
        m.setUsername(m.getUsername() + "-template");
        elasticCustomerOperate.save(m);
        m.setUsername(m.getUsername() + "-repository");
        userRepository.save(m);
    }

    @Override
    public LoginUserInfo<User> veryfiyUser(LoginRequestInfo loginRequestInfo) {
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, loginRequestInfo.getUsername()).last("limit 1"));
        if (user != null && PasswordUtil.getPwd(user.getSalt(), loginRequestInfo.getPassword()).equals(user.getPassword())) {
            return new LoginUserInfo<>(user);
        }
        return new LoginUserInfo<>(user, false);
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
        baseMapper.reduceAccountBalance(id, reduce);
        return ResponseInfo.createSuccess();
    }
}
