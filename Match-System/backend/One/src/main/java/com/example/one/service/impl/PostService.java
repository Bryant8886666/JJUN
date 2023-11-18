package com.example.one.service.impl;

import com.example.one.model.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author Admin
* @description 针对表【post】的数据库操作Service
* @createDate 2023-09-25 14:33:27
*/
public interface PostService extends IService<Post> {

    /**
     * 添加帖子
     * @param post
     * @return post的id
     */
    Long addPost(Post post, HttpServletRequest request);

    /**
     * 检查帖子是否非法，若非法则直接抛出异常
     * @param post
     */
    void validPost(Post post);

}
