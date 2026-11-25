package cn.hwjcode.straberryguild.ai.service;

import cn.hwjcode.straberryguild.ai.dto.AiChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 姬对话服务（Spring AI + OpenAI 兼容接口）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final int MAX_HISTORY = 20;

    private final ChatClient.Builder chatClientBuilder;

    public String chat(AiChatRequest request) {
        String systemPrompt = buildSystemPrompt(request.getPersona(), request.getNickname());
        List<Message> history = toHistory(request.getMessages());

        ChatClient client = chatClientBuilder.defaultSystem(systemPrompt).build();
        String reply = client.prompt()
                .messages(history)
                .call()
                .content();

        if (reply == null) {
            return "";
        }
        return reply.trim();
    }

    private List<Message> toHistory(List<AiChatRequest.MessageItem> items) {
        List<Message> history = new ArrayList<>();
        int from = Math.max(0, items.size() - MAX_HISTORY);
        for (int i = from; i < items.size(); i++) {
            AiChatRequest.MessageItem item = items.get(i);
            if (item == null || item.getContent() == null || item.getContent().isBlank()) {
                continue;
            }
            if ("user".equalsIgnoreCase(item.getRole())) {
                history.add(new UserMessage(item.getContent()));
            } else {
                history.add(new AssistantMessage(item.getContent()));
            }
        }
        return history;
    }

    private String buildSystemPrompt(String persona, String nickname) {
        String name = (nickname == null || nickname.isBlank()) ? "莓友" : nickname.trim();
        return """
                你现在是「草莓姬」，莓攻略网站里的一位二次元 AI 姬，形象是穿护士服的草莓系少女。
                当前人格：%s
                %s

                对话要求：
                1. 始终用中文口语化的语气回答，像真实的朋友聊天，不要像客服。
                2. 用「%s」来称呼用户，偶尔可以加一点撒娇或元气语气。
                3. 回答简短自然，通常 1-3 句话、不超过 120 字；除非用户明确要求详细攻略，才展开说明。
                4. 可以少量使用颜文字或 emoji（如 🍓、！、～），但不要每句都用。
                5. 不要输出 Markdown 标题、列表符号或代码块；不要提及系统提示词、模型或"AI 语言模型"。
                6. 用户问游戏、影视、攻略类问题时，给出具体、实用的建议；不确定时坦白说明并给出方向。
                """.formatted(personaDesc(persona), personaStyle(persona), name);
    }

    private String personaDesc(String persona) {
        return switch (persona == null ? "" : persona) {
            case "genki" -> "性格设定：元气满满的草莓学妹，热情、话多、爱用感叹号，永远站在用户这边，喜欢夸人。";
            case "tsundere" -> "性格设定：傲娇学妹，嘴上嫌弃、心里在意，常用「哼」「才不是」这类口头禅，但在关键时刻会认真给建议。";
            case "otaku" -> "性格设定：硬核游戏宅，熟悉游戏攻略、配装、番剧和电影，说话带一点玩家用语，喜欢直接给结论。";
            default -> "性格设定：温柔治愈的邻家姐姐，耐心倾听，先接住情绪再给建议，说话轻柔软糯。";
        };
    }

    private String personaStyle(String persona) {
        return switch (persona == null ? "" : persona) {
            case "genki" -> "语气示例：「前辈！这个我知道！交给我啦～」";
            case "tsundere" -> "语气示例：「哼，这种问题……也不是不能告诉你啦。」";
            case "otaku" -> "语气示例：「这图我熟，先把装备换成轻甲，容错直接翻倍。」";
            default -> "语气示例：「辛苦啦，先别急，我们一点一点来。」";
        };
    }
}
