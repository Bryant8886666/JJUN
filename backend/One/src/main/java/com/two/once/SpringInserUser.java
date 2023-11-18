package com.two.once;

import com.two.mapper.UserMapper;
import com.two.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

//@Component
public class SpringInserUser {

    @Resource
    private UserMapper userMapper;

    /**
     * 循环插入用户
     * fixedRate = Long.MAX_VALUE代表执行频率是Long的最大值
     */
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        System.out.println("111");
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("yusha");
            user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
            user.setProfile("一条咸鱼");
            user.setGender("男");
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("shayu-yusha@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userMapper.insert(user);
//        }
            stopWatch.stop();
            System.out.println(stopWatch.getLastTaskTimeMillis());

        }
    }}
