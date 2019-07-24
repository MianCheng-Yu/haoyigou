package com.miaoshaproject.redis.test.controller;

import com.miaoshaproject.redis.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationTests {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void test() {
        redisUtil.set("key1", 2, 30);
        redisUtil.set("key2", 123);
        System.out.println(redisUtil.get("key1"));
    }
}
