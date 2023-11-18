package com.two.once;

import com.two.mapper.UserMapper;
import com.two.model.domain.User;
import com.two.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class SpringInserUserTest {

    //线程设置
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;


    /**
     * 循环插入用户
     * fixedRate = Long.MAX_VALUE代表执行频率是Long的最大值
     */
    @Test
    void doInsertUser() {
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

            }
    }



    /**
     * 并发批量插入用户   100000  耗时： 26830ms
     */
    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。（我这里随便写的）
        for (int i = 0; i < INSERT_NUM/batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("假用户");
                user.setUserAccount("yusha");
                user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
                user.setProfile("一条咸鱼");
                user.setGender("女");
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("shayu-yusha@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0 ){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println( stopWatch.getLastTaskTimeMillis());

    }


}