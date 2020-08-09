package com.lxl.auth;

import com.alibaba.fastjson.JSON;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AuthApplicationTests extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository repository;

    @Test
    public void findUsers() {
        System.out.println(JSON.toJSONString(userService.findAll()));
        System.out.println(JSON.toJSONString(repository.findAll()));
    }

}
