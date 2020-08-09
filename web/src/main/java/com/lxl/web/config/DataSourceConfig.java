package com.lxl.web.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.OracleKeyGenerator;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.lxl.web.handler.BaseEntityHandler;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
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
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * 更新公共字段
     * 方式一：@TableField(fill = FieldFill.INSERT) @TableField(fill = FieldFill.INSERT_UPDATE)
     * 方式二：在MetaObjectHandler 继承类的重写方法insertFill或者updateFill中
     *
     * @return
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new BaseEntityHandler();
    }

    /**
     * 主键生成
     *
     * @return
     */
    @Bean
    public IKeyGenerator iKeyGenerator() {
        return new OracleKeyGenerator();
    }
}
