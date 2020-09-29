package com.lxl.auth.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lxl.auth.service.AuthService;
import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.common.enums.CodeEnum;
import com.lxl.common.enums.TagsEnum;
import com.lxl.common.feign.message.MessageFeign;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.annotations.Logined;
import com.lxl.web.redis.RedisCacheUtils;
import com.lxl.web.annotations.Log;
import com.lxl.web.mq.ProducerDeal;
import com.lxl.web.mq.RocketMqConsumer;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.lxl.web.utils.HttpServletUtils.getRequest;

@Api(value = "auth")
@RestController
@RequestMapping("/auth")
public class AuthController implements ProducerDeal {
    private final static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private RocketMqConsumer rocketMqConsumer;

    @Autowired
    private RedisCacheUtils redisCacheUtils;

    @Autowired
    private AuthService authService;

    /**
     * 服务器降级限流
     * value：资源名称
     * entryType：entry 类型，可选项（默认为 EntryType.OUT）
     * blockHandler / blockHandlerClass: blockHandler 函数 public，返回类型需要与原方法相匹配，参数类型需要和原方法相匹配并且最后加一个额外的参数，类型为 BlockException
     * 更多参数 https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
     *
     * @return
     */
//    @SentinelResource(value = "test", blockHandler = "handleException", blockHandlerClass = ExceptionUtil.class)
    @SentinelResource(value = "test", blockHandler = "handleException")
    @HystrixCommand(commandKey = "test")
    //	限流策略：线程池方式
//    @HystrixCommand(
//            commandKey = "test",
//            commandProperties = {
//                    @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD")
//            },
//            threadPoolKey = "testThreadPool",
//            threadPoolProperties = {
//                    @HystrixProperty(name = "coreSize", value = "3"),
//                    @HystrixProperty(name = "maxQueueSize", value = "5"), // 最大排队长度。默认-1，使用SynchronousQueue。其他值则使用 LinkedBlockingQueue
//                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "7") // 排队线程数量阈值，默认为5，达到时拒绝,maxQueueSize=-1失效
//            },
//            fallbackMethod = "fallbackMethod"
//    )
    @GetMapping("/test")
    @Log
    public ResponseInfo test() {
//        try {
//            Thread.sleep(4000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        logger.info(Thread.currentThread().getName() + "执行 auth 访问...");
        redisCacheUtils.getRedisTemplate().opsForValue().increment("test", 1);
        System.out.println("redis 取得test值=" + redisCacheUtils.getCacheObject("test"));
//        rocketMqConsumer.sendTransactionMsg("mq调用测试", MqTagsEnum.TEST);
//        return messageFeign.test();
        return ResponseInfo.createSuccess();
    }

    /**
     * blockHandlerClass没有时，走此处
     *
     * @param ex
     * @return
     */
    public ResponseInfo handleException(BlockException ex) {
        logger.error("限流异常：", ex);
        return ResponseInfo.createCodeEnum(CodeEnum.FLOW_ERROR).setMessage("哎呀，坚持不住了...");
    }

    public ResponseInfo fallbackMethod() {
        return ResponseInfo.createCodeEnum(CodeEnum.FLOW_ERROR).setMessage("HystrixCommand...");
    }

    @Override
    public boolean supportTag(String tagsEnum) {
        return TagsEnum.TEST.getTagName().equals(tagsEnum);
    }

    @Override
    public boolean excute(String msg) {
        return true;
    }

    @Override
    public boolean check(String msg) {
        // 模拟成功
        return true;
    }

    @RequestMapping(value = "/isLogined")
    @ResponseBody
    @Logined
    public ResponseInfo isLogined() {
        return new ResponseInfo(true);
    }

    /***
     * 用户登录
     * @return
     */
    @PostMapping(value = "/login")
    @ResponseBody
    public ResponseInfo login(@RequestBody LoginRequestInfo loginRequestInfo) {
        return authService.login(loginRequestInfo);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @RequestMapping(value = "/logout")
    @ResponseBody
    public ResponseInfo logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(getRequest().getHeader("Authorization"));
    }
}

