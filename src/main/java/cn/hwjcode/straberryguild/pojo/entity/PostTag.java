package cn.hwjcode.straberryguild.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子-标签关联实体类
 * 对应数据库表: post_tag
 */
@Data
@TableName("post_tag")
public class PostTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long postId;

    private Long tagId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}