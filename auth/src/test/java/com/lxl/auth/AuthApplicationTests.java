package com.lxl.auth;

import com.alibaba.fastjson.JSON;
import com.lxl.auth.elastic.UserRepository;
import com.lxl.auth.service.UserService;
import com.lxl.common.entity.auth.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AuthApplicationTests extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository repository;

    @Test
    public void findUsers() {
        List<User> list = userService.findAll();
        repository.deleteAll();
        repository.saveAll(list);
        System.out.println(JSON.toJSONString(repository.findAll()));
    }

}
