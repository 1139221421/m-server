package com.lxl.auth.spider;

import com.alibaba.fastjson.JSON;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;

import java.util.ArrayList;
import java.util.List;

public class WeiboPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    public static void main(String[] args) {
        Spider.create(new WeiboPageProcessor()).addUrl("https://m.weibo.cn/api/container/getIndex?containerid=102803_ctg1_8999_-_ctg1_8999_home").addPipeline(new WeiboPipeline()).run();
        List<WeiboData> list = WeibodataList.getDatas();
        System.out.println(JSON.toJSONString(list));
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        List<String> urls = new ArrayList<String>();
        // 200页 4000条数据左右
        for (int i = 2; i <= 200; i++) {
            urls.add("https://m.weibo.cn/api/container/getIndex?containerid=102803_ctg1_8999_-_ctg1_8999_home&page=" + i);
        }
        page.addTargetRequests(urls);
        String pagestring = page.getRawText();
        if (pagestring.length() > 100) {
            List<String> list = new JsonPathSelector("$.data.cards[*].mblog").selectList(pagestring);
            page.putField("data", list);
        }
    }
}
