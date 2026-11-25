package cn.hwjcode.straberryguild.notification.service;

import cn.hwjcode.straberryguild.pojo.entity.Notification;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    /**
     * 分页查询当前用户的通知
     */
    List<Notification> listByUser(Long userId, int page, int size);

    /**
     * 未读数量
     */
    long countUnread(Long userId);

    /**
     * 标记单条已读
     */
    void markRead(Long userId, Long notificationId);

    /**
     * 全部标记已读
     */
    void markAllRead(Long userId);

    /**
     * 管理员发送站内通知（可选附带邮件）
     * toUserIds 为空时发送给所有用户
     */
    void adminSend(String title, String content, List<Long> toUserIds, boolean withEmail);

    /**
     * 返回结构：{ list, total, unread }
     */
    Map<String, Object> listWithUnread(Long userId, int page, int size);
}
