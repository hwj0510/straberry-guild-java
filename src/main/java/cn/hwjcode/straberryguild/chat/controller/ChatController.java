package cn.hwjcode.straberryguild.chat.controller;

import cn.hwjcode.straberryguild.chat.service.ChatService;
import cn.hwjcode.straberryguild.common.results.Result;
import cn.hwjcode.straberryguild.pojo.vo.ConversationVO;
import cn.hwjcode.straberryguild.pojo.vo.MessageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 私聊 REST 接口（历史会话/消息/已读/未读；发送主路径是 WebSocket，这里提供 REST 兜底）
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** 当前用户的会话列表 */
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations(@AuthenticationPrincipal Long userId) {
        return Result.ok(chatService.listConversations(userId));
    }

    /** 当前用户与 peerId 的消息（分页，正序） */
    @GetMapping("/messages")
    public Result<List<MessageVO>> messages(@AuthenticationPrincipal Long userId,
                                            @RequestParam Long peerId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        return Result.ok(chatService.listMessages(userId, peerId, page, size));
    }

    /** 当前用户未读消息总数 */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(@AuthenticationPrincipal Long userId) {
        return Result.ok(Map.of("count", chatService.countUnread(userId)));
    }

    /** 标记收到的来自 peerId 的消息为已读 */
    @PutMapping("/read")
    public Result<Void> read(@AuthenticationPrincipal Long userId, @RequestParam Long peerId) {
        chatService.markRead(userId, peerId);
        return Result.ok(null);
    }

    /** REST 兜底发送（与 WebSocket 共用 service，对方在线会实时收到） */
    @PostMapping("/send")
    public Result<MessageVO> send(@AuthenticationPrincipal Long userId,
                                  @Valid @RequestBody SendRequest request) {
        return Result.ok(chatService.sendMessage(userId, request.toUserId, request.content));
    }

    @Data
    public static class SendRequest {
        @NotNull(message = "接收人不能为空")
        private Long toUserId;

        @NotBlank(message = "内容不能为空")
        @Size(max = 2000, message = "消息最多 2000 字")
        private String content;
    }
}
