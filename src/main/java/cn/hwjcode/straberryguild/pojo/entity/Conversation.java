package cn.hwjcode.straberryguild.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信会话（双方共用一条记录，user_a_id < user_b_id 保证唯一）
 */
@Data
@TableName("conversation")
public class Conversation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 较小的用户 ID */
    private Long userAId;

    /** 较大的用户 ID */
    private Long userBId;

    private Long lastMessageId;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
