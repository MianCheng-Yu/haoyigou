package com.miaoshaproject.redis.test.controller;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.redis.RedisUtil;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class SpringbootApplicationTests {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void contextLoads() {
        UserDO category = new UserDO();

        category.setId(1);
        category.setName("lala");
        redisUtil.lSet("list", category);
        Category c = (Category) redisUtil.lGetIndex("list", 2);
        System.out.println(c);
    }
}