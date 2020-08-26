package com.lxl.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxl.auth.dao.UserMapper;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.IUserService;
import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.auth.vo.LoginUserInfo;
import com.lxl.common.constance.Constance;
import com.lxl.common.entity.auth.User;
import com.lxl.common.entity.message.Message;
import com.lxl.common.enums.CodeEnum;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.common.feign.message.MessageFeign;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.utils.common.PasswordUtil;
import com.lxl.web.annotations.DisLockDeal;
import com.lxl.web.elastic.ElasticCustomerOperate;
import com.lxl.web.support.CrudServiceImpl;
import io.seata.core.context.RootContext;
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

    /**
     * 余额校验和冻结(需要考虑并发)
     *
     * @param id
     * @param reduce
     * @return
     */
    @Override
    @DisLockDeal(tag = MqTagsEnum.REDUCE_ACCOUNT_BALANCE, lock = "#p1")
    public boolean tccReduceAccountBalancePrepare(Long id, BigDecimal reduce) {
        log.info("分布式事务seata-tcc模拟下单，检查账户余额操作，xid：{}", RootContext.getXID());
        Long balance = (Long) redisCacheUtils.hGet(Constance.User.ACCOUNT_BALANCE, id.toString());
        if (balance == null) {
            // 初始化账户余额
            balance = initAccountBalance(id);
        }
        long m = reduce.multiply(new BigDecimal(100)).longValue();
        if (balance == null || balance < m) {
            // 检查余额是否充足
            throw new RuntimeException("账户余额不足");
        }
        // 冻结账户余额
        redisCacheUtils.hSet(Constance.User.ACCOUNT_BALANCE, id.toString(), balance - m);
        return true;
    }

    @Override
    public boolean tccReduceAccountBalanceCommit(BusinessActionContext actionContext) {
        log.info("分布式事务seata-tcc模拟下单，提交账户余额操作，xid：{}", actionContext.getXid());
        return true;
    }

    @Override
    public boolean tccReduceAccountBalanceRollback(BusinessActionContext actionContext) {
        log.info("分布式事务seata-tcc模拟下单失败，回滚账户余额操作，xid：{}", actionContext.getXid());
        return true;
    }

    /**
     * 初始化账户余额
     *
     * @param id
     */
    private Long initAccountBalance(Long id) {
        User user = findById(id);
        if (user != null) {
            long balance = user.getAccountBalance().multiply(new BigDecimal(100)).longValue();
            redisCacheUtils.hSet(Constance.User.ACCOUNT_BALANCE, id.toString(), balance);
            return balance;
        }
        return null;
    }
}
