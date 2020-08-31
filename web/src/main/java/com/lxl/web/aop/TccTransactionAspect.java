package com.lxl.web.aop;

import com.lxl.common.enums.TransactionEnum;
import com.lxl.web.annotations.TccVerify;
import com.lxl.web.redis.RedisCacheUtils;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * seata-tcc切面验证各个阶段
 * 解决问题:
 * 空回滚：Try未执行，Cancel 执行了
 * 幂等：多次调用方法（Confirm）
 * 悬挂：Cancel接口 比 Try接口先执行
 */
@Slf4j
@Aspect
@Component
@ConditionalOnBean(RedisCacheUtils.class)
public class TccTransactionAspect extends AspectBase {
    @Autowired
    private RedisCacheUtils redisCacheUtils;

    @Value("${spring.application.name:}")
    private String applicationName;

    private static final String TCC_XID_CACHE = "tcc_xid_cache";

    @Around(value = "@annotation(com.lxl.web.annotations.TccVerify)")
    public Object tccVerifyBusiness(ProceedingJoinPoint pjp) throws Throwable {
        Method currentMethod = currentMethod(pjp);
        TccVerify tccVerify = currentMethod.getAnnotation(TccVerify.class);
        String txId = RootContext.getXID();
        if (txId == null && pjp.getArgs() != null && pjp.getArgs().length > 0) {
            Object arg = pjp.getArgs()[0];
            if (arg instanceof BusinessActionContext) {
                BusinessActionContext context = (BusinessActionContext) arg;
                txId = context.getXid();
            }
        }
        TransactionEnum transactionEnum = tccVerify.transaction();
        log.info("分布式事务seata-tcc-prepare验证操作，stage：{}，xid：{}", transactionEnum.getDesc(), txId);
        boolean b;
        if (transactionEnum.getState() == TransactionEnum.PREPARE.getState()) {
            b = prepareVerify(txId);
        } else if (transactionEnum.getState() == TransactionEnum.COMMIT.getState()) {
            b = commitVerify(txId);
        } else {
            b = rollbackVerify(txId);
        }
        if (b) {
            b = (boolean) pjp.proceed();
            if (b) {
                redisCacheUtils.hSet(TCC_XID_CACHE, applicationName + txId, transactionEnum.getState());
            }
            return b;
        }
        return true;
    }

    public boolean prepareVerify(String txId) {
        if (redisCacheUtils.hExists(TCC_XID_CACHE, applicationName + txId)) {
            log.info("分布式事务seata-tcc-prepare验证，检测到事务已回滚，stage：{}，xid：{}", TransactionEnum.PREPARE.getDesc(), txId);
            return false;
        }
        return true;
    }

    public boolean commitVerify(String txId) {
        if (!redisCacheUtils.hExists(TCC_XID_CACHE, applicationName + txId)) {
            log.info("分布式事务seata-tcc-commit验证，未检测到事务，stage：{}，xid：{}", TransactionEnum.COMMIT.getDesc(), txId);
            return false;
        }
        if (redisCacheUtils.hGet(TCC_XID_CACHE, applicationName + txId, Integer.class) == TransactionEnum.COMMIT.getState()) {
            log.info("分布式事务seata-tcc-commit验证，检查到事务重复提交，stage：{}，xid：{}", TransactionEnum.COMMIT.getDesc(), txId);
            return false;
        }
        return true;
    }

    public boolean rollbackVerify(String txId) {
        if (!redisCacheUtils.hExists(TCC_XID_CACHE, applicationName + txId)) {
            log.info("分布式事务seata-tcc-rollback验证，未检测到事务，stage：{}，xid：{}", TransactionEnum.ROLLBACK.getDesc(), txId);
            redisCacheUtils.hSet(TCC_XID_CACHE, applicationName + txId, TransactionEnum.ROLLBACK.getState());
            return false;
        }
        if (redisCacheUtils.hGet(TCC_XID_CACHE, applicationName + txId, Integer.class) == TransactionEnum.ROLLBACK.getState()) {
            log.info("分布式事务seata-tcc-rollback验证，检查到事务重复回滚，stage：{}，xid：{}", TransactionEnum.ROLLBACK.getDesc(), txId);
            return false;
        }
        return true;
    }

}
