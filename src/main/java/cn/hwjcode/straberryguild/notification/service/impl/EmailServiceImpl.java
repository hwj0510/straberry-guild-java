package cn.hwjcode.straberryguild.notification.service.impl;

import cn.hwjcode.straberryguild.notification.service.EmailService;
import cn.hwjcode.straberryguild.pojo.entity.User;
import cn.hwjcode.straberryguild.user.mapper.UserMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserMapper userMapper;
    private final NotificationServiceImpl notificationService;

    @Value("${spring.mail.username:}")
    private String sender;

    @Override
    public void sendSimpleMail(String to, String subject, String content) throws MessagingException {
        if (!isConfigured()) {
            log.warn("SMTP 未配置，跳过邮件发送 -> {}", to);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(sender);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        mailSender.send(msg);
        log.info("邮件发送成功 to={}, subject={}", to, subject);
    }

    @Override
    public void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        if (!isConfigured()) {
            log.warn("SMTP 未配置，跳过邮件发送 -> {}", to);
            return;
        }
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
        helper.setFrom(sender);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(mime);
        log.info("HTML 邮件发送成功 to={}, subject={}", to, subject);
    }

    @Override
    public void sendNotificationWithEmail(Long userId, String title, String content, String type) {
        Long notifId = notificationService.createNotification(userId, title, content, type, false);

        User user = userMapper.selectById(userId);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank() || !isConfigured()) {
            return;
        }
        try {
            String nickname = user.getNickname() != null ? user.getNickname() : user.getUsername();
            String html = buildNotificationHtml(nickname, title, content);
            sendHtmlMail(user.getEmail(), "【莓攻略】" + title, html);
            notificationService.markEmailSent(notifId);
        } catch (Exception e) {
            log.warn("邮件发送失败 userId={}, email={}, error={}", userId, user.getEmail(), e.getMessage());
        }
    }

    public void batchSendWithEmail(List<Long> userIds, String title, String content, String type) {
        for (Long uid : userIds) {
            try {
                sendNotificationWithEmail(uid, title, content, type);
            } catch (Exception e) {
                log.error("批量通知失败 userId={}", uid, e);
            }
        }
    }

    @Override
    public void batchSendAdmin(List<Long> userIds, String title, String content) {
        List<Long> ids = userIds;
        if (ids == null || ids.isEmpty()) {
            ids = userMapper.selectList(null).stream().map(User::getId).toList();
        }
        for (Long uid : ids) {
            try {
                User u = userMapper.selectById(uid);
                if (u == null || u.getEmail() == null || u.getEmail().isBlank() || !isConfigured()) {
                    continue;
                }
                String html = buildNotificationHtml(
                        u.getNickname() != null ? u.getNickname() : u.getUsername(),
                        title, content);
                sendHtmlMail(u.getEmail(), "【莓攻略】" + title, html);
            } catch (Exception e) {
                log.warn("管理员邮件发送失败 userId={}", uid, e);
            }
        }
    }

    private boolean isConfigured() {
        return sender != null && !sender.isBlank();
    }

    private String buildNotificationHtml(String nickname, String title, String content) {
        return "<div style=\"font-family:'PingFang SC',Arial,sans-serif;color:#333;max-width:560px;margin:0 auto;\">" +
                "<div style=\"background:linear-gradient(135deg,#5b8def,#9b59b6);color:#fff;padding:18px 24px;border-radius:8px 8px 0 0;\">" +
                "<h2 style=\"margin:0;font-size:18px;\">莓攻略</h2></div>" +
                "<div style=\"padding:20px 24px;border:1px solid #eceff1;border-top:0;border-radius:0 0 8px 8px;\">" +
                "<p style=\"font-size:15px;margin:0 0 8px;\">你好，" + nickname + "：</p>" +
                "<h3 style=\"margin:0 0 12px;color:#1f2329;font-size:16px;\">" + title + "</h3>" +
                "<p style=\"white-space:pre-wrap;line-height:1.7;margin:0;\">" + content + "</p>" +
                "<p style=\"margin-top:20px;color:#8a919f;font-size:12px;\">此邮件由草莓攻略系统自动发送。</p>" +
                "</div></div>";
    }
}
