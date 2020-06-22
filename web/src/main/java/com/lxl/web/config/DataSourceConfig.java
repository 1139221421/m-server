package com.lxl.web.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.lxl.web.handler.BaseEntityHandler;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * seata数据源搭理配置
 * 增加配置项 spring.cloud.alibaba.seata.tx-service-group=my_test_tx_group
 * 启动类禁用Springboot的dataSources自动装配 @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
 */
@Configuration
public class DataSourceConfig {

    /**
     * 配置数据源
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource() {
        // Druid数据源
        //return new DataSource();
        // HikariCP数据源，注意是JdbcUrl
        return new HikariDataSource();
    }

    /**
     * seata必须设置数据源代理
     * 参考 https://blog.csdn.net/qq_34988304/article/details/105363960
     *
     * @return
     */
    @Primary
    @Bean("dataSource")
    public DataSourceProxy dataSource(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSourceProxy dataSourceProxy) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();

        // 配置mybatis-plus的分页
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        Interceptor[] plugins = {paginationInterceptor};
        sqlSessionFactoryBean.setPlugins(plugins);

        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:/mapper/*.xml"));

        // 配置spring的本地事务
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());

        // 配置mybatis-plus的log打印
        MybatisConfiguration cfg = new MybatisConfiguration();
        cfg.setJdbcTypeForNull(JdbcType.NULL);
        cfg.setMapUnderscoreToCamelCase(true);
        cfg.setCacheEnabled(false);
        cfg.setLogImpl(StdOutImpl.class);
        sqlSessionFactoryBean.setConfiguration(cfg);

        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new BaseEntityHandler();
    }
}
