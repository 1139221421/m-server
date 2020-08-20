package com.lxl.web.interceptor;

import com.lxl.utils.common.SpringContextUtils;
import com.lxl.utils.config.ConfUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean(BaseAuthInterceptor.class)
    public BaseAuthInterceptor authInterceptor() {
        return new DefaultAuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        BaseAuthInterceptor authInterceptor = SpringContextUtils.getBean(BaseAuthInterceptor.class);
        registry.addInterceptor(authInterceptor).addPathPatterns("/" + ConfUtil.getPropertyOrDefault("spring.application.name", "") + "/**");
    }
}
