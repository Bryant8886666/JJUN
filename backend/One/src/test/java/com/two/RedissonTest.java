package com.two;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // list，数据存在本地 JVM 内存中
        List<String> list = new ArrayList<>();
//        list.add("list");
//        System.out.println("list:" + list.get(0));

//        list.remove(0);

        // 数据存在 redis 的内存中
        RList<String> rList = redissonClient.getList("test-list");//这里的值就是所谓的key标识的字段
//        rList.add("redis");
        System.out.println("rlist:" + rList.get(0));
        rList.remove(0);

        // map
//        Map<String, Integer> map = new HashMap<>();
//        map.put("lk;l", 10);
//        map.get("map");
//
//        RMap<Object, Object> map1 = redissonClient.getMap("test-map");

        // set

        // stack


    }
}