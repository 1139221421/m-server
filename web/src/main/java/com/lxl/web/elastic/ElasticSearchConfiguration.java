package com.lxl.web.elastic;

import com.lxl.utils.config.ConfUtil;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.io.Serializable;


@EnableElasticsearchRepositories(basePackages = "com.lxl.**.elastic") // repository方式
@Configuration
public class ElasticSearchConfiguration implements Serializable {
    private static final long serialVersionUID = 266562315484321215L;

    @Bean
    public String indexSuffix() {
        return ConfUtil.getPropertyOrDefault("spring.elasticsearch.indexSuffix", "");
    }

    /**
     * ElasticsearchRestTemplate 方式
     *
     * @param restHighLevelClient
     * @return
     */
    @Bean
    public ElasticCustomerOperate elasticsearchTemplate(RestHighLevelClient restHighLevelClient) {
        return new ElasticCustomerOperate(restHighLevelClient);
    }
}
