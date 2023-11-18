package com.two;

import com.two.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("String", "dog");
        valueOperations.set("Int", 1);
        valueOperations.set("", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("yu");
        valueOperations.set("yuUser", user);
        // 查
        Object yu = valueOperations.get("String");
        Assertions.assertTrue("dog".equals((String) yu));
        yu = valueOperations.get("Int");
        Assertions.assertTrue(1 == (Integer) yu);
        yu = valueOperations.get("Double");
        Assertions.assertTrue(2.0 == (Double) yu);
        System.out.println(valueOperations.get("User"));
        valueOperations.set("String", "dog");
        redisTemplate.delete("String");
    }
}
