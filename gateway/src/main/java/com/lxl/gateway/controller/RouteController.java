package com.lxl.gateway.controller;

import com.lxl.gateway.service.DynamicRouteService;
import com.lxl.gateway.vo.GatewayFilterDefinition;
import com.lxl.gateway.vo.GatewayPredicateDefinition;
import com.lxl.gateway.vo.GatewayRouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态网关配置 待完成点：动态设置的网关配置未保存。
 **/
@RestController
@RequestMapping("/route")
public class RouteController {
    @Autowired
    private DynamicRouteService dynamicRouteService;

    /**
     * 增加路由
     *
     * @param gwdefinition
     * @return
     */
    @PostMapping("/add")
    public String add(@RequestBody GatewayRouteDefinition gwdefinition) {
        try {
            RouteDefinition definition = this.assembleRouteDefinition(gwdefinition);
            return this.dynamicRouteService.add(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "succss";
    }

    /**
     * 删除路由
     *
     * @param id
     * @return
     */
    @DeleteMapping("/routes/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable String id) {
        return this.dynamicRouteService.delete(id);
    }

    /**
     * 更新路由
     *
     * @param gwdefinition
     * @return
     */
    @PostMapping("/update")
    public String update(@RequestBody GatewayRouteDefinition gwdefinition) {
        RouteDefinition definition = this.assembleRouteDefinition(gwdefinition);
        return this.dynamicRouteService.update(definition);
    }

    private RouteDefinition assembleRouteDefinition(GatewayRouteDefinition gwdefinition) {
        RouteDefinition definition = new RouteDefinition();
        List<PredicateDefinition> pdList = new ArrayList<>();
        List<FilterDefinition> filterDefinitionListList = new ArrayList<>();
        definition.setId(gwdefinition.getId());
        List<GatewayPredicateDefinition> gatewayPredicateDefinitionList = gwdefinition.getPredicates();
        for (GatewayPredicateDefinition gpDefinition : gatewayPredicateDefinitionList) {
            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setArgs(gpDefinition.getArgs());
            predicate.setName(gpDefinition.getName());
            pdList.add(predicate);
        }
        List<GatewayFilterDefinition> filters = gwdefinition.getFilters();
        for (GatewayFilterDefinition filter : filters) {
            FilterDefinition filterDefinition = new FilterDefinition();
            filterDefinition.setArgs(filter.getArgs());
            filterDefinition.setName(filter.getName());
            filterDefinitionListList.add(filterDefinition);
        }
        definition.setPredicates(pdList);
        definition.setFilters(filterDefinitionListList);
        //URI uri = UriComponentsBuilder.fromHttpUrl(gwdefinition.getUri()).build().toUri();
        URI uri = URI.create(gwdefinition.getUri());
        definition.setUri(uri);
        return definition;
    }
}
