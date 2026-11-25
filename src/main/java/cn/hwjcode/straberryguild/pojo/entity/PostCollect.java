package cn.hwjcode.straberryguild.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子收藏记录
 */
@Data
@TableName("post_collect")
public class PostCollect implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
