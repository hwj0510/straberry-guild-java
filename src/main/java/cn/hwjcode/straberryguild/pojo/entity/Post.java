package cn.hwjcode.straberryguild.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子实体类
 * 对应数据库表: post
 */
@Data
@TableName("post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者用户 ID */
    private Long authorId;

    private String title;

    private String content;

    /** 分类，默认"推荐" */
    private String category;

    /** 封面图 */
    private String cover;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    private Integer shareCount;

    private Integer viewCount;

    /** 状态: 1-正常 */
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}