package com.lxl.auth.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.lxl.auth.spider.WeiboData;
import com.lxl.auth.spider.WeiboPageProcessor;
import com.lxl.auth.spider.WeiboPipeline;
import com.lxl.auth.spider.WeibodataList;
import com.lxl.auth.vo.SearchVo;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.elastic.ElasticCustomerOperate;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/weibodata")
public class WeiboDataController {
    @Autowired
    private ElasticCustomerOperate elasticCustomerOperate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @RequestMapping(value = "/sync")
    public ResponseInfo sync() {
        Spider.create(new WeiboPageProcessor()).addUrl("https://m.weibo.cn/api/container/getIndex?containerid" +
                "=102803_ctg1_8999_-_ctg1_8999_home").addPipeline(new WeiboPipeline()).run();
        List<WeiboData> list = WeibodataList.getDatas();
        if (CollectionUtil.isNotEmpty(list)) {
            elasticCustomerOperate.save(list);
        }
        return ResponseInfo.createSuccess();
    }

    @RequestMapping(value = "/search")
    public ResponseInfo search(SearchVo searchVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 排序
        searchSourceBuilder.sort("expireTime", SortOrder.DESC);
        // 分页查询
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                // 单字段
                .must(QueryBuilders.matchQuery("source", "微博视频"))
                // 多字段查询
                .must(QueryBuilders.multiMatchQuery("机器人", "rawText", "text"));
        searchSourceBuilder.query(boolQueryBuilder);
        //高亮配置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("source")
                .requireFieldMatch(false)
                .preTags("<span style=\"color:yellow\">")
                .postTags("</span>")
                .fragmentSize(800000).numOfFragments(0);
        searchSourceBuilder.highlighter(highlightBuilder);
        // 查询请求，可设置多个index
        SearchRequest request = new SearchRequest("weibo_data");
        request.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            return ResponseInfo.createSuccess(hits);
        } catch (IOException e) {
            log.error("es查询报错：", e);
            return ResponseInfo.createError();
        }
    }
}
