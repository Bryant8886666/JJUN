package com.example.one.service.impl;

import com.example.one.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Admin
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2023-09-25 14:09:30
 */
public interface UserService extends IService<User> {

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







//public interface UserService extends IService<User> {
//
//    /**
//     * 添加用户（管理员）
//     * @param user
//     * @return
//     */
//    long userAdd(User user);
//
//    long UserRegister(String userAccount,String userPassword,String checkPassword);
//
//    User UserLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest);
//
//    /**
//     * 用户脱敏
//     *
//     * @param originUser
//     * @return
//     */
//    User getSafetyUser(com.example.one.model.User originUser);
//
//    /**
//     * 用户注销
//     * @param httpServletRequest
//     */
//    int userlogout(HttpServletRequest httpServletRequest);
//
//    void updateLoginStatusToZero(HttpServletRequest httpServletRequest);
//    void updateLoginStatus(HttpServletRequest httpServletRequest);
//    User getUserById(Long id);
//    Long updateUser(Long id,String username,String userAccount,String avatarUrl,String gender,String phone,String email,Integer userRole);
//    //    User getUserByAccount(String userAccount);
//    User getUserByAccount(String Account);
//}
