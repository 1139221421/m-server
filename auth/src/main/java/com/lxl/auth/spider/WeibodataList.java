package com.lxl.auth.spider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 爬虫获取的消息列表
 *
 * @author
 */
public class WeibodataList {

    private static Map<Long, WeiboData> map = new HashMap<>();

    public static void addData(WeiboData data) {
        if (!map.containsKey(data.getId())) {
            map.put(data.getId(), data);
        } else {
            // 重复没做处理
            map.put(data.getId(), data);
        }
    }

    public static WeiboData getData(Long id) {
        if (map.containsKey(id)) {
            return map.remove(id);
        }
        return null;
    }

    public static List<WeiboData> getDatas() {
        return map.values().parallelStream().collect(Collectors.toList());
    }

}
