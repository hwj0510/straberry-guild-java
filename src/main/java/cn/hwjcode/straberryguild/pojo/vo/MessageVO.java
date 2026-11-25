package cn.hwjcode.straberryguild.pojo.vo;

import cn.hwjcode.straberryguild.pojo.entity.PrivateMessage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私聊消息（前后端 / WebSocket 统一载体）
 */
@Data
@Builder
public class MessageVO {

    private Long id;

    private Long conversationId;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private String type;

    private Integer isRead;

    private LocalDateTime createdAt;

    public static MessageVO fromEntity(PrivateMessage m) {
        if (m == null) return null;
        return MessageVO.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .fromUserId(m.getFromUserId())
                .toUserId(m.getToUserId())
                .content(m.getContent())
                .type(m.getType())
                .isRead(m.getIsRead())
                .createdAt(m.getCreateTime())
                .build();
    }
}
