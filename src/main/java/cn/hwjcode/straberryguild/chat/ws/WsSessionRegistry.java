package cn.hwjcode.straberryguild.chat.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线 WebSocket session 注册表：按 userId 维护该用户的所有连接（多端/多标签页）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsSessionRegistry {

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<Long, Set<Session>> sessions = new ConcurrentHashMap<>();

    public void register(Long userId, Session session) {
        sessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(Long userId, Session session) {
        Set<Session> set = sessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessions.remove(userId);
            }
        }
    }

    public boolean isOnline(Long userId) {
        Set<Session> set = sessions.get(userId);
        return set != null && !set.isEmpty();
    }

    /** 推送 JSON 给某用户的所有在线 session */
    public void sendToUser(Long userId, Object payload) {
        Set<Session> set = sessions.get(userId);
        if (set == null || set.isEmpty()) {
            return;
        }
        String text;
        try {
            text = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("WS 消息序列化失败", e);
            return;
        }
        for (Session s : set) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(text);
                } catch (IOException e) {
                    log.warn("WS 推送失败 userId={}", userId, e);
                }
            }
        }
    }
}
