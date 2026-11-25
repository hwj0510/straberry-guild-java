package cn.hwjcode.straberryguild.chat.service;

import cn.hwjcode.straberryguild.pojo.vo.ConversationVO;
import cn.hwjcode.straberryguild.pojo.vo.MessageVO;

import java.util.List;

public interface ChatService {

    /** 当前用户的会话列表（聚合对方信息、最后消息、未读数） */
    List<ConversationVO> listConversations(Long me);

    /** 当前用户与某 peer 的消息（正序，分页） */
    List<MessageVO> listMessages(Long me, Long peer, int page, int size);

    /** 当前用户未读消息总数 */
    long countUnread(Long me);

    /** 标记当前用户收到的来自 peer 的消息为已读 */
    void markRead(Long me, Long peer);

    /** 发送消息：落库 + 推送给收发双方（实时由 WebSocket 完成） */
    MessageVO sendMessage(Long fromId, Long toId, String content);
}
