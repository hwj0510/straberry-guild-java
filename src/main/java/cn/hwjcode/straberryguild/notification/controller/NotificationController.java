package cn.hwjcode.straberryguild.notification.controller;

import cn.hwjcode.straberryguild.common.results.Result;
import cn.hwjcode.straberryguild.notification.service.EmailService;
import cn.hwjcode.straberryguild.notification.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailService emailService;

    /** 当前用户的通知列表 + 未读数 */
    @GetMapping
    public Result<Map<String, Object>> list(@AuthenticationPrincipal Long userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(notificationService.listWithUnread(userId, page, size));
    }

    /** 当前用户未读数 */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(@AuthenticationPrincipal Long userId) {
        Map<String, Object> resp = Map.of("count", notificationService.countUnread(userId));
        return Result.ok(resp);
    }

    /** 标记单条已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        notificationService.markRead(userId, id);
        return Result.ok(null);
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllRead(userId);
        return Result.ok(null);
    }

    /**
     * 管理员发送通知 + 邮件
     * 非管理员调用会返回 403
     */
    @PostMapping("/admin/send")
    public Result<Void> adminSend(@AuthenticationPrincipal Long userId,
                                  @Valid @RequestBody AdminSendRequest request) {
        // 1. 写站内通知
        List<Long> recipients = request.userIds;
        notificationService.adminSend(request.title, request.content, recipients, false);

        // 2. 发邮件（如果开启）
        if (request.withEmail != null && request.withEmail) {
            // recipients 为空表示所有用户，此时让 EmailService 内部处理
            List<Long> ids = (recipients == null || recipients.isEmpty())
                    ? null : recipients;
            emailService.batchSendAdmin(ids, request.title, request.content);
        }
        return Result.ok(null, "发送成功");
    }

    @Data
    public static class AdminSendRequest {
        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题不能超过 100 字")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        /** 接收用户 ID 列表；null/空 表示所有用户 */
        private List<Long> userIds;

        /** 是否同时发邮件 */
        private Boolean withEmail = true;
    }
}
