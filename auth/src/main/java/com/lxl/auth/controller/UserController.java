package com.lxl.auth.controller;

import com.lxl.auth.service.UserService;
import com.lxl.common.entity.auth.User;
import com.lxl.common.vo.ResponseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/user")
public class UserController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseInfo create(@RequestBody User user) {
        userService.crate(user);
        return ResponseInfo.createSuccess(userService.findAll());
    }

    @PostMapping("/update")
    public ResponseInfo update(@RequestBody User user) {
        userService.update(user);
        return ResponseInfo.createSuccess(userService.findAll());
    }

    @PostMapping("/delete/{id}")
    public ResponseInfo delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseInfo.createSuccess(userService.findAll());
    }

    @GetMapping("/reduceAccountBalance")
    public ResponseInfo reduceAccountBalance(@RequestParam("id") Long id, @RequestParam("reduce") BigDecimal reduce) {
        return userService.reduceAccountBalance(id, reduce);
    }

}

