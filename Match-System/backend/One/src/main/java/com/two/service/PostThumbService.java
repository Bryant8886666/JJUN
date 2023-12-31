package com.two.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.two.model.domain.PostThumb;

/**
* @author Admin
* @description 针对表【post_thumb(帖子点赞记录)】的数据库操作Service
* @createDate 2023-09-25 15:44:03
*/
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞/取消点赞
     * @param userId
     * @param postId
     * @return
     */
    long doThumb(long userId, long postId);

}
