package com.lxl.auth.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.lxl.auth.spider.WeiboData;
import com.lxl.auth.spider.WeiboPageProcessor;
import com.lxl.auth.spider.WeiboPipeline;
import com.lxl.auth.spider.WeibodataList;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.elastic.ElasticCustomerOperate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;

import java.util.List;

@RestController
@RequestMapping("/weibodata")
public class WeiboDataController {
    @Autowired
    private ElasticCustomerOperate elasticCustomerOperate;

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

}
