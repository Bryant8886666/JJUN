package com.two.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.two.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author Admin
 * @description 针对表【user(用户)】的数据库操作Service
 */
public interface UserService extends IService<User> {


    boolean isAdmin(HttpServletRequest request);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

    /**
     * 是否为管理员
     *
     * @param
     * @return
     */
    boolean isAdmin(User loginUser);


    /**
     * 更新用户
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);


    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTagsBySQL(List<String> tagNameList);



    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 用户密码修改
     */
    String updateUserPassword(Long userId, String newPassword);




    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户信息脱敏
     * @param loginUser
     * @return
     */
    User getSafetyUser(User loginUser);

    /**
     * 用户注销
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 添加用户（管理员）
     * @param user
     * @return
     */
    long userAdd(User user);



}







