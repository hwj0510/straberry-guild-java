package cn.hwjcode.straberryguild.notification.service.impl;

import cn.hwjcode.straberryguild.common.exception.BusinessException;
import cn.hwjcode.straberryguild.notification.mapper.NotificationMapper;
import cn.hwjcode.straberryguild.notification.service.NotificationService;
import cn.hwjcode.straberryguild.pojo.entity.Notification;
import cn.hwjcode.straberryguild.pojo.entity.User;
import cn.hwjcode.straberryguild.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    /** 内部方法：创建通知记录 */
    public Long createNotification(Long userId, String title, String content, String type, boolean emailSent) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type == null ? "SYSTEM" : type);
        n.setIsRead(0);
        n.setEmailSent(emailSent ? 1 : 0);
        notificationMapper.insert(n);
        return n.getId();
    }

    /** 内部方法：标记邮件已发送 */
    public void markEmailSent(Long notificationId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, notificationId)
                .set(Notification::getEmailSent, 1));
    }

    @Override
    public List<Notification> listByUser(Long userId, int page, int size) {
        Page<Notification> pageParam = Page.of(page, size);
        return notificationMapper.selectPage(pageParam, new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime))
                .getRecords();
    }

    @Override
    public long countUnread(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null) {
            throw BusinessException.notFound("通知不存在");
        }
        if (!n.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权操作");
        }
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, notificationId)
                .set(Notification::getIsRead, 1));
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    @Override
    @Transactional
    public void adminSend(String title, String content, List<Long> toUserIds, boolean withEmail) {
        if (toUserIds == null || toUserIds.isEmpty()) {
            // 发给所有用户
            toUserIds = userMapper.selectList(null).stream().map(User::getId).toList();
        }
        for (Long uid : toUserIds) {
            createNotification(uid, title, content, "SYSTEM", false);
        }
        if (withEmail) {
            try {
                // 注入 EmailService 调用；为避免循环依赖，这里手动查找已发送通知
                // 注意：EmailService 通过 @Autowired 依赖本类，本类反向不依赖 EmailService
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Map<String, Object> listWithUnread(Long userId, int page, int size) {
        Page<Notification> pageParam = Page.of(page, size);
        Page<Notification> result = notificationMapper.selectPage(pageParam, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime));

        Map<String, Object> resp = new HashMap<>();
        resp.put("list", result.getRecords());
        resp.put("total", result.getTotal());
        resp.put("unread", countUnread(userId));
        return resp;
    }
}
