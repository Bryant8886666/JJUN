package com.two.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.two.common.ErrorCode;
import com.two.exception.BusinessException;
import com.two.mapper.PostMapper;
import com.two.model.domain.Post;
import com.two.model.domain.User;
import com.two.service.PostService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import static com.example.one.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Admin
* @description 针对表【post】的数据库操作Service实现
* @createDate 2023-09-25 14:33:27
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService {


    /**
     * 不雅词汇数组
     */
    private static final String[] PROFANITY_WORDS = {
            "操你妈",
            "滚你妈",
            "吃屎",
            "去死",
            "鸡巴",

    };

    @Override
    public Long addPost(Post post, HttpServletRequest request) {
        if(post == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //检查用户是否登录
        User loginUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"请先登录");
        }
        post.setUserId(loginUser.getId());
        //检查帖子是否合法
        validPost(post);
        //保存帖子
        boolean saveResult = this.save(post);
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子信息保存失败");
        }
        return post.getId();
    }

    @Override
    public void validPost(Post post) {
        String content = post.getContent();
        if(StringUtils.isAnyBlank(post.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子内容不能为空");
        }
        if(content.length() > 8192){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帖子内容过长");
        }
        if(containsProfanity(content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请文明发言");
        }
    }

    /**
     * 检测是否包含不文明词汇
     * @param content
     * @return
     */
    public static boolean containsProfanity(String content) {
        for (String word : PROFANITY_WORDS) {
            if (content.toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }

}




