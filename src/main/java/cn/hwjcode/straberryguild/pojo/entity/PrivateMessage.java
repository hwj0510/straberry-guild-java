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
 * 私信消息明细
 */
@Data
@TableName("private_message")
public class PrivateMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private String type;

    /** 0-未读 1-已读 */
    private Integer isRead;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
