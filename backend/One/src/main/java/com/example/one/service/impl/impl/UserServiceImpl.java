package com.example.one.service.impl.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.one.common.ErrorCode;
import com.example.one.exception.BusinessException;
import com.example.one.model.User;
import com.example.one.mapper.UserMapper;
import com.example.one.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.one.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Admin
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-09-25 14:09:30
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，用于对密码加密
     */
    public static final String SALT = "DOG";


    @Override
    public String updateUserPassword(Long userId, String newPassword) {
        // 对新密码进行加密
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());

        // 更新用户的密码
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        user.setUserPassword(encryptedPassword);
        userMapper.updateById(user);
        return encryptedPassword;
    }


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号长度过短");

        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码长度过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号包含特殊字符");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码两次不相等");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        Long integer = userMapper.selectCount(queryWrapper);
        if(integer > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号已存在");
        }
        //2.对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户信息保存失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号长度过短");
        }
        if(userPassword.length() < 8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码长度过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号包含特殊字符");
        }
        // 对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //2.检验账号和密码是否匹配
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码错误");
        }
        //3.用户信息脱敏
        User safetyUser = getSafetyUser(user);
        //4.记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;

    }

    @Override
    public User getSafetyUser(User loginUser) {
        if(loginUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(loginUser.getId());
        safetyUser.setUsername(loginUser.getUsername());
        safetyUser.setUserAccount(loginUser.getUserAccount());
        safetyUser.setAvatarUrl(loginUser.getAvatarUrl());
        safetyUser.setGender(loginUser.getGender());
        safetyUser.setAge(loginUser.getAge());
        safetyUser.setGrade(loginUser.getGrade());
        safetyUser.setColleage(loginUser.getColleage());
        safetyUser.setMajor(loginUser.getMajor());
        safetyUser.setInterest(loginUser.getInterest());
        safetyUser.setUserStatus(loginUser.getUserStatus());
        safetyUser.setPhone(loginUser.getPhone());
        safetyUser.setContactinfo(loginUser.getContactinfo());
        safetyUser.setCreateTime(loginUser.getCreateTime());
        safetyUser.setUserRole(loginUser.getUserRole());
//        修改后




        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public long userAdd(User user) {
        //1.校验
        if(StringUtils.isAnyBlank(user.getUserAccount(),user.getUserPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(user.getUserAccount().length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号长度过短");

        }
        if(user.getUserPassword().length() < 8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码长度过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(user.getUserAccount());
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号包含特殊字符");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",user.getUserAccount());
        Long integer = userMapper.selectCount(queryWrapper);
        if(integer > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号已存在");
        }
        //2.设置默认密码
//        user.setUserPassword("12345678");
        //3.对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes());
        //4.插入数据
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户信息保存失败");
        }
        return user.getId();
    }
}










//@Service
//@Slf4j
//public class UserServiceImpl extends ServiceImpl<UserMapper, User>
//    implements UserService {
//    @Resource
//    private UserMapper userMapper;
//    private final String SAFE ="DOG";
//
//    @Override
//    public long userAdd(User user) {
//        //1.校验
//        if(StringUtils.isAnyBlank(user.getUserAccount(),user.getUserPassword())){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
//        }
//        if(user.getUserAccount().length() < 4){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号长度过短");
//
//        }
//        if(user.getUserPassword().length() < 8 ){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码长度过短");
//        }
//        // 账户不能包含特殊字符
//        String validPattern = "\\pP|\\pS|\\s+";
//        Matcher matcher = Pattern.compile(validPattern).matcher(user.getUserAccount());
//        if (matcher.find()) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号包含特殊字符");
//        }
//
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_account",user.getUserAccount());
//        Long integer = userMapper.selectCount(queryWrapper);
//        if(integer > 0){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号已存在");
//        }
//        //2.设置默认密码
//        user.setUserPassword("12345678");
//        //3.对密码加密
//        String encryptPassword = DigestUtils.md5DigestAsHex((SAFE + user.getUserPassword()).getBytes());
//        //4.插入数据
//        user.setUserPassword(encryptPassword);
//        boolean saveResult = this.save(user);
//        if(!saveResult){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户信息保存失败");
//        }
//        return user.getId();
//    }
//
//    @Override
//    public long UserRegister(String userAccount, String userPassword, String checkPassword) {
//        if (StringUtils.isAnyEmpty(userAccount,userPassword,checkPassword)){
//            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数为空");
//        }
//        if (userAccount.length()<4){
//            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户账户太短");
//        }
//        if (userPassword.length()<8||checkPassword.length()<8){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入得密码不匹配");
//        }
////        账户不能包含特殊字符
//        String SpecialCharacter="[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
//        Matcher matcher =Pattern.compile(SpecialCharacter).matcher(userAccount);
//        if(matcher.find()){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");
//        }
//
////        账户不能重复
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.eq("userAccount", userAccount);
//        long count = userMapper.selectCount(userQueryWrapper);
//        if (count>0){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户已存在");
//        }
//
//        String SafePassword = DigestUtils.md5DigestAsHex((SAFE+userPassword).getBytes(StandardCharsets.UTF_8));
//        User user = new User();
//        user.setUserAccount(userAccount);
//        user.setUserPassword(SafePassword);
//        boolean saveresult=this.save(user);
//        if (!saveresult){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"保存失败");
//        }
//        return user.getId();
//
//
//    }
//
//    @Override
//    public User UserLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
//
//        if (StringUtils.isAnyEmpty(userAccount,userPassword)){
//            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
//        }
//        if (userAccount.length()<4){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户长度小于4");
//        }
//        if (userPassword.length()<8){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户密码长度小于8");
//        }
////        账户不能包含特殊字符
//        String SpecialCharacter="[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
//        Matcher matcher =Pattern.compile(SpecialCharacter).matcher(userAccount);
//        if(matcher.find()){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");
//        }
//        String SafePassword = DigestUtils.md5DigestAsHex((SAFE+userPassword).getBytes(StandardCharsets.UTF_8));
//
////        查询账户是否存在
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.eq("userAccount", userAccount);
//        userQueryWrapper.eq("userPassword",SafePassword);
//        long count = userMapper.selectCount(userQueryWrapper);
//        User user = userMapper.selectOne(userQueryWrapper);
//        if (user==null){
//            log.info("user with password don't match");
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码账号不匹配");
//        }
//
//        // 3. 用户脱敏
//        User safetyUser = getSafetyUser(user);
//        // 4. 记录用户的登录态
//        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
//        log.info("登录成功");
//        return safetyUser;
//    }
//
//    @Override
//    public User getSafetyUser(User originUser) {
//        if (originUser==null){
//            return null;
//        }
//        User safetyUser = new User();
//        safetyUser.setId(originUser.getId());
//        safetyUser.setUsername(originUser.getUsername());
//        safetyUser.setUserAccount(originUser.getUserAccount());
//        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
//        safetyUser.setGender(originUser.getGender());
//        safetyUser.setPhone(originUser.getPhone());
//        safetyUser.setEmail(originUser.getEmail());
//        safetyUser.setPhone(originUser.getPhone());
//        safetyUser.setAge(originUser.getAge());
//        safetyUser.setColleage(originUser.getColleage());
//        safetyUser.setContactinfo(originUser.getContactinfo());
//        safetyUser.setInterest(originUser.getInterest());
//        safetyUser.setMajor(originUser.getMajor());
//        safetyUser.setUserRole(originUser.getUserRole());
//        safetyUser.setUserStatus(originUser.getUserStatus());
//        safetyUser.setCreateTime(originUser.getCreateTime());
//        return safetyUser;
//    }
//
//    @Override
//    public int userlogout(HttpServletRequest httpServletRequest) {
//        // 移除登录态
//        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
//        return 1;
//    }
//
//    @Override
//    public void updateLoginStatusToZero(HttpServletRequest httpServletRequest) {
//        User user=(User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
//        if (user!=null){
//            user.setUserStatus(0);
//            userMapper.updateById(user);
//        }
//    }
//
//    @Override
//    public void updateLoginStatus(HttpServletRequest httpServletRequest) {
//        User user=(User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
//        if (user!=null){
//            user.setUserStatus(1);
//            userMapper.updateById(user);
//        }
//    }
//
//    @Override
//    public User getUserById(Long id) {
//        return userMapper.selectById(id);
//
//    }
//
//    @Override
//    public Long updateUser(Long id, String username, String userAccount, String avatarUrl, String gender, String phone, String email, Integer userRole) {
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("id",id);
//        User user=userMapper.selectOne(queryWrapper);
//        user.setUsername(username);
//        user.setUserAccount(userAccount);
//        user.setAvatarUrl(avatarUrl);
//        user.setGender(gender);
//        user.setPhone(phone);
//        user.setEmail(email);
//        int result=userMapper.update(user,queryWrapper);
//        if (result<=0){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新用户信息失败");
//        }
//        return id;
//    }
//
//    @Override
//    public User getUserByAccount(String userAccount) {
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("userAccount", userAccount);
//        return userMapper.selectOne(queryWrapper);
//    }
//
//
//}
//
//
//
//
