package cn.hwjcode.straberryguild.notification.service;

import jakarta.mail.MessagingException;

import java.util.List;

/**
 * 邮件发送服务
 */
public interface EmailService {

    /**
     * 发送简单文本邮件
     */
    void sendSimpleMail(String to, String subject, String content) throws MessagingException;

    /**
     * 发送 HTML 邮件
     */
    void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException;

    /**
     * 批量发送站内通知 + 邮件
     * 收件人未配置邮箱时仅发站内通知
     */
    void sendNotificationWithEmail(Long userId, String title, String content, String type);
    /**
     * 批量发送管理员通知邮件
     * userIds 为 null 表示所有用户
     */
    void batchSendAdmin(List<Long> userIds, String title, String content);
}
