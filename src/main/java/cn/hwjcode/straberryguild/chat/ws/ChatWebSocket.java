package cn.hwjcode.straberryguild.chat.ws;

import cn.hwjcode.straberryguild.chat.service.ChatService;
import cn.hwjcode.straberryguild.pojo.vo.MessageVO;
import cn.hwjcode.straberryguild.security.JwtUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 私聊 WebSocket 端点
 * 连接：ws(s)://host/ws/chat?token=<accessToken>
 * 客户端 -> 服务端消息：
 *   {"type":"ping"}
 *   {"type":"message","toUserId":2,"content":"你好"}
 *   {"type":"read","peerId":2}
 * 服务端 -> 客户端消息：
 *   {"type":"pong"}
 *   {"type":"message","data":{MessageVO}}   // 发给收发双方，前端按 fromUserId 判断是否自己
 *   {"type":"error","message":"..."}
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat")
public class ChatWebSocket {

    private static JwtUtil jwtUtil;
    private static ChatService chatService;
    private static WsSessionRegistry registry;
    private static ObjectMapper objectMapper;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        ChatWebSocket.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setChatService(ChatService chatService) {
        ChatWebSocket.chatService = chatService;
    }

    @Autowired
    public void setRegistry(WsSessionRegistry registry) {
        ChatWebSocket.registry = registry;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        ChatWebSocket.objectMapper = objectMapper;
    }

    @OnOpen
    public void onOpen(Session session) {
        String token = extractToken(session.getQueryString());
        if (StrUtil.isBlank(token) || !jwtUtil.validateToken(token)) {
            closeQuietly(session, CloseReason.CloseCodes.VIOLATED_POLICY, "未授权");
            return;
        }
        Long userId = jwtUtil.getUserId(token);
        session.getUserProperties().put("userId", userId);
        registry.register(userId, session);
        log.info("WS 建立连接 userId={}", userId);
    }

    @OnMessage
    public void onMessage(String text, Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId == null) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(text);
            String type = node.path("type").asText("");
            switch (type) {
                case "ping" -> registry.sendToUser(userId, Map.of("type", "pong"));
                case "message" -> handleMessage(userId, node);
                case "read" -> {
                    long peer = node.path("peerId").asLong(0);
                    if (peer > 0) {
                        chatService.markRead(userId, peer);
                    }
                }
                default -> log.debug("未知 WS 消息类型: {}", type);
            }
        } catch (Exception e) {
            log.warn("WS 消息处理失败: {}", e.getMessage());
        }
    }

    private void handleMessage(Long fromId, JsonNode node) {
        long toId = node.path("toUserId").asLong(0);
        String content = node.path("content").asText("");
        if (toId <= 0 || StrUtil.isBlank(content)) {
            return;
        }
        try {
            MessageVO vo = chatService.sendMessage(fromId, toId, content);
            if (vo != null) {
                registry.sendToUser(vo.getToUserId(), Map.of("type", "message", "data", vo));
                registry.sendToUser(vo.getFromUserId(), Map.of("type", "message", "data", vo));
            }
        } catch (Exception e) {
            registry.sendToUser(fromId, Map.of("type", "error", "message", e.getMessage()));
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            registry.unregister(userId, session);
        }
        log.info("WS 关闭连接 userId={}", userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("WS error userId={}: {}", session.getUserProperties().get("userId"), error.getMessage());
    }

    private String extractToken(String query) {
        if (StrUtil.isBlank(query)) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    private void closeQuietly(Session session, CloseReason.CloseCode code, String reason) {
        try {
            session.close(new CloseReason(code, reason));
        } catch (Exception ignored) {
        }
    }
}