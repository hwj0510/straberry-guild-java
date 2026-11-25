package cn.hwjcode.straberryguild.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表项（含对方信息 + 最后消息 + 未读数）
 */
@Data
public class ConversationVO {

    private Long conversationId;

    private Long peerId;

    private String peerName;

    private String peerAvatar;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Long unread;
}
