package cn.hwjcode.straberryguild.chat.service.impl;

import cn.hwjcode.straberryguild.chat.mapper.ConversationMapper;
import cn.hwjcode.straberryguild.chat.mapper.PrivateMessageMapper;
import cn.hwjcode.straberryguild.chat.service.ChatService;
import cn.hwjcode.straberryguild.common.exception.BusinessException;
import cn.hwjcode.straberryguild.pojo.entity.Conversation;
import cn.hwjcode.straberryguild.pojo.entity.PrivateMessage;
import cn.hwjcode.straberryguild.pojo.entity.User;
import cn.hwjcode.straberryguild.pojo.vo.ConversationVO;
import cn.hwjcode.straberryguild.pojo.vo.MessageVO;
import cn.hwjcode.straberryguild.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final PrivateMessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    public List<ConversationVO> listConversations(Long me) {
        if (me == null) {
            return List.of();
        }
        return conversationMapper.selectConversations(me);
    }

    @Override
    public List<MessageVO> listMessages(Long me, Long peer, int page, int size) {
        if (me == null || peer == null || peer <= 0) {
            return List.of();
        }
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        long offset = (long) (p - 1) * s;
        List<PrivateMessage> list = messageMapper.selectConversationMessages(me, peer, offset, s);
        // SQL 按时间倒序取页，这里翻转为正序（旧 -> 新）展示
        List<PrivateMessage> asc = new ArrayList<>(list);
        Collections.reverse(asc);
        return asc.stream().map(MessageVO::fromEntity).toList();
    }

    @Override
    public long countUnread(Long me) {
        if (me == null) {
            return 0;
        }
        return messageMapper.countUnread(me);
    }

    @Override
    @Transactional
    public void markRead(Long me, Long peer) {
        if (me == null || peer == null || peer <= 0) {
            return;
        }
        messageMapper.markRead(me, peer);
    }

    @Override
    @Transactional
    public MessageVO sendMessage(Long fromId, Long toId, String content) {
        if (fromId == null || toId == null) {
            throw BusinessException.badRequest("参数错误");
        }
        if (fromId.equals(toId)) {
            throw BusinessException.badRequest("不能给自己发消息");
        }
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw BusinessException.badRequest("消息内容不能为空");
        }
        if (text.length() > 2000) {
            throw BusinessException.badRequest("消息过长（最多 2000 字）");
        }
        User toUser = userMapper.selectById(toId);
        if (toUser == null) {
            throw BusinessException.notFound("对方用户不存在");
        }

        Conversation conv = getOrCreateConversation(fromId, toId);

        PrivateMessage msg = new PrivateMessage();
        msg.setConversationId(conv.getId());
        msg.setFromUserId(fromId);
        msg.setToUserId(toId);
        msg.setContent(text);
        msg.setType("text");
        msg.setIsRead(0);
        messageMapper.insert(msg);

        // 更新会话最后消息（展示用，最多存 200 字）
        String preview = text.length() > 200 ? text.substring(0, 200) : text;
        conversationMapper.update(null, new LambdaUpdateWrapper<Conversation>()
                .eq(Conversation::getId, conv.getId())
                .set(Conversation::getLastMessageId, msg.getId())
                .set(Conversation::getLastMessageContent, preview)
                .set(Conversation::getLastMessageTime, LocalDateTime.now()));

        return MessageVO.fromEntity(msg);
    }

    /** 双方归一化为 user_a_id < user_b_id，保证会话唯一 */
    private Conversation getOrCreateConversation(Long a, Long b) {
        long x = Math.min(a, b);
        long y = Math.max(a, b);
        Conversation conv = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserAId, x)
                .eq(Conversation::getUserBId, y));
        if (conv == null) {
            conv = new Conversation();
            conv.setUserAId(x);
            conv.setUserBId(y);
            conversationMapper.insert(conv);
        }
        return conv;
    }
}
