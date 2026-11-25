package cn.hwjcode.straberryguild.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体类
 * 对应数据库表: tag
 */
@Data
@TableName("tag")
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer postCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}