package com.lxl.auth.spider;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

/**
 * 获取data并保存
 *
 * @author
 */
public class WeiboPipeline implements Pipeline {

    @SuppressWarnings("unchecked")
    @Override
    public void process(ResultItems resultItems, Task task) {
        List<String> data = resultItems.get("data");
        if (CollectionUtil.isNotEmpty(data)) {
            data.parallelStream().forEach(item -> WeibodataList.addData(JSON.parseObject(item, WeiboData.class)));
            System.out.println(JSON.toJSONString(resultItems));
            System.out.println(JSON.toJSONString(task));
        }
    }
}
