package com.two.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.two.common.BaseResponse;
import com.two.common.ErrorCode;
import com.two.common.ResultUtil;
import com.two.exception.BusinessException;
import com.two.model.domain.User;
import com.two.model.request.UserLoginRequest;
import com.two.model.request.UserRegisterRequest;
import com.two.model.request.UserSearchRequest;
import com.two.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.two.constant.UserConstant.ADMIN_ROLE;
import static com.two.constant.UserConstant.USER_LOGIN_STATE;

//@CrossOrigin(origins = "http://localhost:3000/#",allowCredentials = "true")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
     private UserService userService;



    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtil.success(userService.matchUsers(num, user));
    }

    /**
     * 推荐页面
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User logininUser = userService.getLoginUser(request);
        String redisKey = String.format("shayu:user:recommend:%s",logininUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读取
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null){
            return ResultUtil.success(userPage);
        }
        //无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        //写缓存,30s过期,
        try {
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResultUtil.success(userPage);
    }


    /**
     * 更新用户信息
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user,loginUser);
        return ResultUtil.success(result);
    }

    //根据标签查询用户
    @GetMapping("/searchByTags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            return ResultUtil.error(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtil.success(userList);
    }



    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
//            return ResultUtil.error(ErrorCode.PARAMS_ERROR);
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtil.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw  new BusinessException(ErrorCode.NULL_ERROR,"请求参数错误");

        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        log.info(userAccount);
        log.info(userPassword);
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账号密码不能为空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtil.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            throw  new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtil.success(result);
    }

//    JSESSIONID=FCBF1451993FFF40DD82419B736CEE69

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object object = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) object;
        if(user == null){
            throw  new BusinessException(ErrorCode.NOT_LOGIN,"未登录");
        }
        //从数据库中获取用户信息，以保证信息的实时性
        Long id = user.getId();
        user = userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtil.success(safetyUser);
    }

    @GetMapping("/search/{username}")
    private BaseResponse<List<User>> searchUsers(@PathVariable String username,HttpServletRequest request){
        //仅管理员可查询
        if(!isAdmin(request)){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        //用户信息脱敏
        List<User> list = userList.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtil.success(list);
    }

    @GetMapping("/list")
    private BaseResponse<List<User>> listUsers(HttpServletRequest request){
//        修改前
        //仅管理员可查询
        if(!isAdmin(request)){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        List<User> list = userService.list();
        //用户信息脱敏
        List<User> safetyList = list.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtil.success(safetyList);
//修改后
//        if (isAdmin(request)) {
//            List<User> list = userService.list();
//            return ResultUtil.success(list);
//        } else {
//            List<User> list = userService.list();
//            // 用户信息脱敏
//            List<User> safetyList = list.stream().map(user -> {
//                return userService.getSafetyUser(user);
//            }).collect(Collectors.toList());
//            return ResultUtil.success(safetyList);
//        }

    }

    @GetMapping("/search2")
    public BaseResponse<List<User>> searchUsers(UserSearchRequest searchRequest, HttpServletRequest request) {
        // 管理员校验
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        String username = searchRequest.getUsername();
        String userAccount = searchRequest.getUserAccount();
        Integer gender = searchRequest.getGender();
        String phone = searchRequest.getPhone();
        String email = searchRequest.getEmail();
        Integer userStatus = searchRequest.getUserStatus();
        Integer userRole = searchRequest.getUserRole();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //Date updateTime = searchRequest.getUpdateTime();
        Date createTime = searchRequest.getCreateTime();
        // username
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        // userAccount
        if (StringUtils.isNotBlank(userAccount)) {
            queryWrapper.like("userAccount", userAccount);
        }
        // gender
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.eq("gender", gender);
        }
        // phone
        if (StringUtils.isNotBlank(phone)) {
            queryWrapper.like("phone", phone);
        }
        // email
        if (StringUtils.isNotBlank(email)) {
            queryWrapper.like("email", email);
        }
        // userStatus
        if (userStatus != null) {
            queryWrapper.eq("userStatus", userStatus);
        }

        if (userRole != null) {
            queryWrapper.eq("userRole", userRole);
        }

//        if (updateTime != null) {
//            queryWrapper.like("updateTime", updateTime);
//        }
        if (createTime != null) {
            queryWrapper.like("createTime", createTime);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> users = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
        return ResultUtil.success(users);
    }


    @DeleteMapping("/delete/{id}")
    private BaseResponse<Boolean> deleteUser(@PathVariable long id, HttpServletRequest request){
        //仅管理员可删除
        if(!isAdmin(request)){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtil.success(b);
    }

    @PutMapping("/update/{id}")
    private BaseResponse<Boolean> updateUser(@PathVariable long id,@RequestBody User user,HttpServletRequest request){
        //仅管理员可修改
        if(!isAdmin(request)){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.updateById(user);
        return ResultUtil.success(true);
    }

    @PutMapping("/update")
    private BaseResponse<Boolean> updateCurrentUser(@RequestBody User user,HttpServletRequest request){
        User loginUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser == null){
            throw  new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = loginUser.getId();
        if(id <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        user.setId(id);
        user.setUserPassword(userService.updateUserPassword(user.getId(),user.getUserPassword()));
        boolean b = userService.updateById(user);
        return ResultUtil.success(true);
    }

    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody User user,HttpServletRequest request){
        //仅管理员可添加用户
        if(!isAdmin(request)){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        if(StringUtils.isAnyBlank(user.getUserAccount(),user.getUserPassword())){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userAdd(user);
        return ResultUtil.success(result);
    }


    /**
     * 是否为管理员
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        //仅管理员可查询
        User user =(User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if(user == null || user.getUserRole() != ADMIN_ROLE){
            return false;
        }
        return true;
    }

}







//@RestController
//@RequestMapping("/user")
//public class UserController {
//    @Resource
//    private UserService userService;
//
//    @PostMapping("/add")
//    public BaseResponse<Long> addUser(@RequestBody User user,HttpServletRequest request){
//        //仅管理员可添加用户
//        if(!isAdmin(request)){
//            throw  new BusinessException(ErrorCode.NO_AUTH);
//        }
//        if(StringUtils.isAnyBlank(user.getUserAccount(),user.getUserPassword())){
//            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        long result = userService.userAdd(user);
//        return ResultUtils.success(result);
//    }
//
//
//    @PutMapping("/update")
//    private BaseResponse<Boolean> updateCurrentUser(@RequestBody User user,HttpServletRequest request){
//        User loginUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
//        if(loginUser == null){
//            throw  new BusinessException(ErrorCode.NOT_LOGIN);
//        }
//        Long id = loginUser.getId();
//        if(id <= 0){
//            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        user.setId(id);
//        boolean b = userService.updateById(user);
//        return ResultUtils.success(true);
//    }
//
//
//    @PutMapping("/update/{id}")
//    private BaseResponse<Boolean> updateUser(@PathVariable long id,@RequestBody User user,HttpServletRequest request){
//        //仅管理员可修改
//        if(!isAdmin(request)){
//            throw  new BusinessException(ErrorCode.NO_AUTH);
//        }
//        if(id <= 0){
//            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        boolean b = userService.updateById(user);
//        return ResultUtils.success(true);
//    }
//
//
//    @GetMapping("/list")
//    private BaseResponse<List<User>> listUsers(HttpServletRequest request){
//        //仅管理员可查询
//        if(!isAdmin(request)){
//            throw  new BusinessException(ErrorCode.NO_AUTH);
//        }
//        List<User> list = userService.list();
//        //用户信息脱敏
//        List<User> safetyList = list.stream().map(user -> {
//            return userService.getSafetyUser(user);
//        }).collect(Collectors.toList());
//        return ResultUtils.success(safetyList);
//    }
//
//
//    /**
//     * 更新用户信息
//     */
//    @PostMapping("/updateUser")
//    public BaseResponse<Long> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
//        if (userUpdateRequest==null){
//            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
//        }
//        Long id=userUpdateRequest.getId();
//        String userName=userUpdateRequest.getUserName();
//        String userAccount=userUpdateRequest.getUserAccount();
//        String avatarUrl=userUpdateRequest.getAvataUrl();
//        String gender=userUpdateRequest.getGender();
//        String Phone=userUpdateRequest.getPhone();
//        String email=userUpdateRequest.getEmail();
//        Integer userRole=userUpdateRequest.getUserRole();
//        // 将用户名前后空格去掉
//        userName = userName.trim();
//        Long result=userService.updateUser(id,userName,userAccount,avatarUrl,gender,Phone,email,userRole);
//        if (StringUtils.isAnyEmpty(id.toString(),userName,userAccount,avatarUrl,gender,Phone,email,userRole.toString())){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能将用户数据修改为空");
//        }
//        return ResultUtils.success(result);
//    }
//
////    /**
////     * 获取当前用户信息
////     */
////
////    @GetMapping("/getCurrent")
////    public BaseResponse<User> SearchUser(String userAccount) {
////        System.out.println("用户账户为" + userAccount);
////        User user = userService.getUserByAccount(userAccount);
////        if (user != null) {
////            User safeUser = userService.getSafetyUser(user);
////            return ResultUtils.success(safeUser);
////        } else {
////            return ResultUtils.error(ErrorCode.NULL_ERROR);
////        }
////    }
//
//
//
//
//
//    /**
//     * 获取当前用户
//     */
////    @PostMapping("/current")
//    @GetMapping("/current")
//    public BaseResponse<User> getCurrentUser(HttpServletRequest httpServletRequest) {
//        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentuser = (User) userObj;
//        if (currentuser == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        }
//        long id = currentuser.getId();
//        User user = userService.getById(id);
//        User safetyuser= userService.getSafetyUser(user);
//        return ResultUtils.success(safetyuser);
//    }
//
//
//    /**
//     * 用户注册
//     */
//    @PostMapping("/register")
//    public BaseResponse<Long> UserRegisterRequest(@RequestBody UserRegisterRequest userRegisterRequest) {
//
//        if (userRegisterRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        String userAccount = userRegisterRequest.getUserAccount();
//        String userPassword = userRegisterRequest.getCheckPassword();
//        String checkPassword = userRegisterRequest.getCheckPassword();
//        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword)) {
//            throw new BusinessException(ErrorCode.NULL_ERROR);
//        }
//        long result= userService.UserRegister(userAccount, userPassword, checkPassword);
//        return ResultUtils.success(result);
//    }
//
//    /**
//     * 用户登录
//     */
//
//    @PostMapping("/login")
//    public BaseResponse<User> UserLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
//
//        if (userLoginRequest == null) {
//            return null;
//        }
//        String userAccount = userLoginRequest.getUserAccount();
//        String userPassword = userLoginRequest.getUserPassword();
//        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
//            throw new BusinessException(ErrorCode.NULL_ERROR);
//        }
//        User user = userService.UserLogin(userAccount, userPassword, httpServletRequest);
//        // 在这里手动设置登录状态字段
//        if (user != null) {
//           userService.updateLoginStatus(httpServletRequest);
//        }
//        return ResultUtils.success(user);
//    }
//
//
//
//    @GetMapping("/search/{username}")
//    public BaseResponse<List<User>> SearchUsers(@PathVariable String username,HttpServletRequest httpServletRequest ){
//        if (!isAdmin(httpServletRequest)){
//            throw new BusinessException(ErrorCode.NO_AUTH);
//
//        }
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        if (StringUtils.isNotEmpty(username)) {
//            userQueryWrapper.like("username", username);
//            System.out.println(userQueryWrapper.like("username", username));
//        }
//        List<User> userList = userService.list(userQueryWrapper);
//
//        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
//        return ResultUtils.success(list);
//    }
//    @DeleteMapping("/delete/{id}")
//    public BaseResponse<Boolean> deleteUser(@PathVariable Long id,HttpServletRequest httpServletRequest){
//        if (!isAdmin(httpServletRequest)){
//            throw new BusinessException(ErrorCode.NO_AUTH);
//        }
//        if (id<=0){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        boolean a = userService.removeById(id);
//        return ResultUtils.success(a);
//
//    }
//
//    /**
//     * 是否为管理员
//     *
//     * @param request
//     * @return
//     */
//    private boolean isAdmin(HttpServletRequest request) {
//        // 仅管理员可查询
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User user = (User) userObj;
//        return user != null && user.getUserRole() == ADMIN_ROLE;
//    }
//
//
//
//    @PostMapping("/outlogin")
//    public BaseResponse<Integer> outLogin( HttpServletRequest httpServletRequest){
//
//        if (httpServletRequest==null){
//            throw new BusinessException(ErrorCode.NULL_ERROR);
//        }
//        userService.updateLoginStatusToZero(httpServletRequest);
//        int result=userService.userlogout(httpServletRequest);
//        return ResultUtils.success(result);
//    }
//
//
//
//
//}
