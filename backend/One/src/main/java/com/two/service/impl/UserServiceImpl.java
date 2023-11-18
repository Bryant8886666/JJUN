package com.two.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.two.common.ErrorCode;
import com.two.constant.UserConstant;
import com.two.exception.BusinessException;
import com.two.mapper.UserMapper;
import com.two.model.domain.User;
import com.two.service.UserService;
import com.two.utils.AlgorithmUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.one.constant.UserConstant.USER_LOGIN_STATE;
import static com.two.constant.UserConstant.ADMIN_ROLE;

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

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        //     选择了运行5万条数据。
//        不然的话会报 OOM（内存）的问题
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.last("limit 50000");
//        List<User> userList = this.list(queryWrapper);
//         或者用page分页查询，自己输入或默认数值，但这样匹配就有限制了
//        List<User> userList = this.page(new Page<>(pageNum,pageSize),queryWrapper);
//		这里查了所有用户，近100万条
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id","tags");
        List<User> userList = this.list(queryWrapper);

        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下表 => 相似度'
        List<Pair<User,Long>> list = new ArrayList<>();
        // 依次计算当前用户和所有用户的相似度
        for (int i = 0; i <userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签的 或当前用户为自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user,distance));
        }
        //按编辑距离有小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        //有顺序的userID列表
        List<Long> userListVo = topUserPairList.stream().map(pari -> pari.getKey().getId()).collect(Collectors.toList());

        //根据id查询user完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id",userListVo);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));

        // 因为上面查询打乱了顺序，这里根据上面有序的userID列表赋值
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userListVo){
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限");
        }
        return (User) userObj;
    }

    /**
     *
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接 and 查询
        //like '%Java%' and like '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2.在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }





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
//        match add
        safetyUser.setTags(loginUser.getTags());
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
