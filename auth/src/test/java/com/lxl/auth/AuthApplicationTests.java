package com.lxl.auth;

import com.alibaba.fastjson.JSON;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.UserService;
import com.lxl.common.entity.auth.User;
import com.lxl.web.elastic.ElasticCustomerOperate;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AuthApplicationTests extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository repository;

    @Autowired
    private ElasticCustomerOperate elasticCustomerOperate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 数据同步
     */
    @Test
    public void syncUsers() {
        elasticCustomerOperate.indexOps(User.class).delete();
        elasticCustomerOperate.createAndPutMapping(User.class);
        List<User> list = userService.findAll();
        repository.saveAll(list);
        Optional<User> optional = repository.findById(1L);
        User user = null;
        if (optional.isPresent()) {
            user = optional.get();
        }
        System.out.println(JSON.toJSONString(repository.findAll()));
    }

    /**
     * searchAfter
     *
     * @throws Exception
     */
    @Test
    public void userSearchAfter() throws Exception {
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource().query(new MatchAllQueryBuilder());
        SearchRequest request = new SearchRequest("user");
        searchSourceBuilder.sort("createTime", SortOrder.DESC);
        searchSourceBuilder.size(10);
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        System.out.println(JSON.toJSONString(hits));

        //将上次查询最后一条记录的排序字段作为searchAfter参数，查询上次结果之后的数据
        // 上次最后一条数据时间戳1581374654000L；注意多个字段排序保持searchAfter传参顺序
        searchSourceBuilder.searchAfter(new Object[]{1581374654000L});
        SearchRequest request1 = new SearchRequest("user");
        request1.source(searchSourceBuilder);
        SearchResponse searchResponse1 = restHighLevelClient.search(request1, RequestOptions.DEFAULT);
        SearchHit[] hits1 = searchResponse1.getHits().getHits();
        System.out.println(JSON.toJSONString(hits1));
    }

    /**
     * 距离范围查询
     *
     * @throws Exception
     */
    @Test
    public void searchDistance() throws Exception {
        double lat = 30.640808;
        double lon = 104.036032;
        //===========查询5千米以内的==========
        // 以location字段这个经纬度为中心，搜索指定范围
        GeoDistanceQueryBuilder distanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        distanceQueryBuilder.point(lat, lon);
        distanceQueryBuilder.distance(5, DistanceUnit.KILOMETERS);
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource().query(QueryBuilders.boolQuery().filter(distanceQueryBuilder));
        // 排序
        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder("location", lat, lon);
        geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS);
        geoDistanceSortBuilder.order(SortOrder.ASC);
        searchSourceBuilder.sort(geoDistanceSortBuilder);

        // 查询
        SearchRequest request = new SearchRequest("area");
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        System.out.println(JSON.toJSONString(hits));

        //===========查询多边形内的地点==========
        List<GeoPoint> points = new ArrayList<>();
        points.add(new GeoPoint(30.681665, 104.029051));
        points.add(new GeoPoint(30.700546, 104.072889));
        points.add(new GeoPoint(30.683901, 104.113995));
        points.add(new GeoPoint(30.645506, 104.114426));
        points.add(new GeoPoint(30.626863, 104.084171));
        points.add(new GeoPoint(30.642896, 104.036166));
        GeoPolygonQueryBuilder polygonQueryBuilder = new GeoPolygonQueryBuilder("location", points);
        SearchSourceBuilder searchSourceBuilder1 = SearchSourceBuilder.searchSource().query(QueryBuilders.boolQuery().filter(polygonQueryBuilder));
        SearchRequest request1 = new SearchRequest("area");
        request1.source(searchSourceBuilder1);
        SearchResponse searchResponse1 = restHighLevelClient.search(request1, RequestOptions.DEFAULT);
        SearchHit[] hits1 = searchResponse1.getHits().getHits();
        System.out.println(JSON.toJSONString(hits1));
    }

    /**
     * 综合查询
     *
     * @throws Exception
     */
    @Test
    public void SearchComprehensive() throws Exception {
        // 位置查询
        double lat = 30.640808;
        double lon = 104.036032;
        String name = "广场";
        String type = "景点";
        String desc = "逛街";

        // 位置查询 并把位置的相对权重调高，默认1
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource();
        GeoDistanceQueryBuilder distanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        distanceQueryBuilder.point(lat, lon);
        distanceQueryBuilder.distance(5, DistanceUnit.KILOMETERS);
        distanceQueryBuilder.boost(2);

        searchSourceBuilder.query(QueryBuilders.boolQuery()
                // 位置查询 must换成filter则不会打分
                .must(distanceQueryBuilder)
                // 名称模糊查询(部分词)
                .must(QueryBuilders.matchPhraseQuery("name", name))
                //类型全匹配 注意：该字段类型必须是keyword，不能是text
                .must(QueryBuilders.termQuery("type", type))
                // 描述关键词匹配（分词）,多个字段用 multiMatchQuery
                .must(QueryBuilders.matchQuery("desc", desc)));

        //高亮配置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder
                //高亮查询字段
                .field("desc")
                .field("type")
                //如果要多个字段高亮,这项要为false
                .requireFieldMatch(false)
                //高亮设置
                .preTags("<span style=\"color:yellow\">")
                .postTags("</span>")
                //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
                //最大高亮分片数
                .fragmentSize(800000)
                //从第一个分片获取高亮片段
                .numOfFragments(0);
        searchSourceBuilder.highlighter(highlightBuilder);

        // 按分值排序
        searchSourceBuilder.sort("_score", SortOrder.DESC);

        SearchRequest request = new SearchRequest("area");
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println("分数：" + hit.getScore() + " 对象：" + JSON.toJSONString(hit.getSourceAsMap()));
        }
    }

}
