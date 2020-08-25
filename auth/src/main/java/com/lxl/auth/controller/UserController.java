package com.lxl.auth.controller;

import com.lxl.auth.service.IUserService;
import com.lxl.common.entity.auth.User;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.support.BaseCrudController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/user")
public class UserController extends BaseCrudController<User, IUserService, Long> {

    @Autowired
    private IUserService userService;

    @Override
    protected IUserService getService() {
        return userService;
    }

    @ResponseBody
    @GetMapping("/reduceAccountBalance")
    public ResponseInfo reduceAccountBalance(@RequestParam("id") Long id, @RequestParam("reduce") BigDecimal reduce) {
        return userService.reduceAccountBalance(id, reduce);
    }

}

