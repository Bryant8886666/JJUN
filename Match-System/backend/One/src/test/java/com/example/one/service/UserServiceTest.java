package com.example.one.service;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.two.model.domain.User;
import com.two.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;




    @Test
    public void testAddUser(){
        User user = new User();
        user.setId(0L);
        user.setUsername("阿松大");
        user.setUserAccount("12312");
        user.setAvatarUrl("");
        user.setGender("男");
        user.setUserPassword("21321");
        user.setPhone("43343");
        user.setEmail("12312");
        boolean result=userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);


    }


}