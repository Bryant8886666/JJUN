package com.two.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.two.model.domain.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName post
 */
@TableName(value ="post")
@Data
public class Post implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 照片地址
     */
    @TableField(value = "photo")
    private String photo;

    /**
     * 状态（0-待审核, 1-通过, 2-拒绝）
     */
    @TableField(value = "reviewStatus")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @TableField(value = "reviewMessage")
    private String reviewMessage;

    /**
     * 浏览数
     */
    @TableField(value = "viewNum")
    private Integer viewNum;

    /**
     * 点赞数
     */
    @TableField(value = "thumbNum")
    private Integer thumbNum;

    /**
     * 创建用户 id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    @TableField(exist = false)
    private User user;
    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Post other = (Post) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getPhoto() == null ? other.getPhoto() == null : this.getPhoto().equals(other.getPhoto()))
            && (this.getReviewStatus() == null ? other.getReviewStatus() == null : this.getReviewStatus().equals(other.getReviewStatus()))
            && (this.getReviewMessage() == null ? other.getReviewMessage() == null : this.getReviewMessage().equals(other.getReviewMessage()))
            && (this.getViewNum() == null ? other.getViewNum() == null : this.getViewNum().equals(other.getViewNum()))
            && (this.getThumbNum() == null ? other.getThumbNum() == null : this.getThumbNum().equals(other.getThumbNum()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getPhoto() == null) ? 0 : getPhoto().hashCode());
        result = prime * result + ((getReviewStatus() == null) ? 0 : getReviewStatus().hashCode());
        result = prime * result + ((getReviewMessage() == null) ? 0 : getReviewMessage().hashCode());
        result = prime * result + ((getViewNum() == null) ? 0 : getViewNum().hashCode());
        result = prime * result + ((getThumbNum() == null) ? 0 : getThumbNum().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", content=").append(content);
        sb.append(", photo=").append(photo);
        sb.append(", reviewStatus=").append(reviewStatus);
        sb.append(", reviewMessage=").append(reviewMessage);
        sb.append(", viewNum=").append(viewNum);
        sb.append(", thumbNum=").append(thumbNum);
        sb.append(", userId=").append(userId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}