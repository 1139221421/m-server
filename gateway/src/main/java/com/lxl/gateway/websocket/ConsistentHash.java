package com.lxl.gateway.websocket;

import com.netflix.loadbalancer.Server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * 一致性hash操作
 */
public class ConsistentHash<T> {
    // 每个机器节点关联的虚拟节点个数
    private final int numberOfReplicas;
    // 环形虚拟节点
    private final SortedMap<Long, T> circle = new TreeMap<Long, T>();

    /**
     * @param numberOfReplicas 每个机器节点关联的虚拟节点个数
     * @param nodes            真实机器节点
     */
    public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            add(node);
        }
    }

    /**
     * 增加真实机器节点
     *
     * @param node
     */
    public void add(T node) {
        for (int i = 0; i < this.numberOfReplicas; i++) {
            circle.put(hash(node.toString() + "#" + i), node);
        }
    }

    /**
     * 删除真实机器节点
     *
     * @param node
     */
    public void remove(T node) {
        for (int i = 0; i < this.numberOfReplicas; i++) {
            circle.remove(hash(node.toString() + "#" + i));
        }
    }

    /**
     * 取得真实机器节点
     *
     * @param key
     * @return
     */
    public T get(String key) {
        if (circle.isEmpty()) {
            return null;
        }

        long hash = hash(key);
        if (!circle.containsKey(hash)) {
            // 沿环的顺时针找到一个虚拟节点，没找到则返回第一个
            SortedMap<Long, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        // 返回该虚拟节点对应的真实机器节点的信息
        return circle.get(hash);
    }

    /**
     * MurMurHash算法，是非加密HASH算法，性能很高，碰撞率低，随机分布特征表现更好
     */
    public static Long hash(String key) {
        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
        int seed = 0x0209BADC;
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        long m = 0xc6a4a7935bd1e995L;
        int r = 47;
        long h = seed ^ (buf.remaining() * m);
        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();
            k *= m;
            k ^= k >>> r;
            k *= m;
            h ^= k;
            h *= m;
        }
        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }
        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;
        buf.order(byteOrder);
        return h;
    }


    public static void main(String[] args) {
        String prefix = "192.168.1.";
        // 每台真实机器节点上保存的记录条数
        Map<String, Integer> map = new HashMap<String, Integer>();
        // 真实机器节点
        ArrayList<Server> nodes = new ArrayList<Server>();
        // 10台真实机器节点集群
        for (int i = 1; i <= 10; i++) {
            // 每台真实机器节点上保存的记录条数初始为0
            map.put(prefix + i, 0);
            nodes.add(new Server("node" + i, prefix + i, 8080));
        }
        // 每台真实机器引入200个虚拟节点
        ConsistentHash<Server> consistentHash = new ConsistentHash<Server>(200, nodes);
        // 将10000条记录尽可能均匀的存储到10台机器节点
        for (int i = 0; i < 10000; i++) {
            // 产生随机一个字符串当做一条记录，可以是其它更复杂的业务对象,比如随机字符串相当于对象的业务唯一标识
            String data = UUID.randomUUID().toString() + i;
            // 通过记录找到真实机器节点
            Server server = consistentHash.get(data);
            // 每台真实机器节点上保存的记录条数加1
            map.put(server.getHost(), map.get(server.getHost()) + 1);
        }

        // 打印每台真实机器节点保存的记录条数
        for (int i = 1; i <= 10; i++) {
            System.out.println(prefix + i + "节点记录条数：" + map.get("192.168.1." + i));
        }
    }
}
