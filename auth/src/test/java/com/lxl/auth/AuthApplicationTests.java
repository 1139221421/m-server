package com.lxl.auth;

import com.alibaba.fastjson.JSON;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.IUserService;
import com.lxl.common.entity.auth.User;
import com.lxl.web.elastic.ElasticCustomerOperate;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
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
    private IUserService userService;

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
        List<User> list = userService.list();
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
    public void useSearchAfter() throws Exception {
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource().query(new MatchAllQueryBuilder());
        SearchRequest request = new SearchRequest("user");
        searchSourceBuilder.sort("createTime", SortOrder.DESC);
        searchSourceBuilder.size(10);
        // 聚合查询 查询平均年龄
        AvgAggregationBuilder aggregation = AggregationBuilders.avg("avg_age").field("age");
        searchSourceBuilder.aggregation(aggregation);
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        Avg avg = searchResponse.getAggregations().get("avg_age");
        System.out.println("平均年龄：" + avg.getValue());
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
        double lat = 30.640808;
        double lon = 104.036032;
        String name = "广场";
        String type = "景点";
        String desc = "美食购物";

        // 位置查询
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource();
        GeoDistanceQueryBuilder distanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        distanceQueryBuilder.point(lat, lon);
        distanceQueryBuilder.distance(5, DistanceUnit.KILOMETERS);

        searchSourceBuilder.query(QueryBuilders.boolQuery()
                // 位置查询
                .filter(distanceQueryBuilder)
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

        // 按分值排序 默认 _score DESC
        // searchSourceBuilder.sort("_score", SortOrder.DESC);
        // 查询超时
        // searchSourceBuilder.timeout(TimeValue.timeValueMinutes(2L));
        // 分页量
        // searchSourceBuilder.size(10);

        SearchRequest request = new SearchRequest("area");
        // 默认QUERY_THEN_FETCH：打分时只参考本分片，数据少的时候不够准确
        // DFS_QUERY_THEN_FETCH:增加了预查询处理，询问term和document frequency，评分更准确，性能更差
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        request.source(searchSourceBuilder);
        // 设置指定查询的路由分片
        // request.routing("routing");
        // 用preference方法去指定优先去某个分片上去查询（默认的是随机先去某个分片）
        // request.preference("_local");
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println("分数：" + hit.getScore() + " 对象：" + JSON.toJSONString(hit.getSourceAsMap()));
        }
    }

}
