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
import org.elasticsearch.search.SearchHits;
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
        Spider.create(new WeiboPageProcessor())
                .addUrl("https://m.weibo.cn/api/container/getIndex?containerid=102803_ctg1_8999_-_ctg1_8999_home")
                .addPipeline(new WeiboPipeline())
                .run();
        List<WeiboData> list = WeibodataList.getDatas();
        if (CollectionUtil.isNotEmpty(list)) {
            elasticCustomerOperate.save(list);
        }
        return ResponseInfo.createSuccess();
    }

    @RequestMapping(value = "/search")
    public ResponseInfo search(SearchVo searchVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("createTime", SortOrder.DESC);
        searchSourceBuilder.sort("informationCode.keyword", SortOrder.DESC);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //查询当前机构的
        boolQueryBuilder.must(QueryBuilders.matchQuery("orgId", 1))
                //状态都为可用
                .must(QueryBuilders.matchQuery("state", 1));
        //高亮配置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("activityName")
                .field("informationName")
                .field("tableName")
                .field("informationContent")
                .requireFieldMatch(false)
                .preTags("<span style=\"color:yellowgreen\">").postTags("</span>")
                //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
                .fragmentSize(800000)
                .numOfFragments(0);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        SearchRequest request = new SearchRequest("weibo_data");
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            return ResponseInfo.createSuccess(hits);
        } catch (IOException e) {
            log.error("es查询报错：", e);
            return ResponseInfo.createError();
        }
    }
}
