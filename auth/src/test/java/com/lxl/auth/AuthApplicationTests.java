package com.lxl.auth;

import com.alibaba.fastjson.JSON;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.UserService;
import com.lxl.web.elastic.ElasticCustomerOperate;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void findUsers() {
//        List<User> list = userService.findAll();
//        repository.deleteAll();
//        repository.saveAll(list);
        System.out.println(JSON.toJSONString(repository.findAll()));
    }

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

}
