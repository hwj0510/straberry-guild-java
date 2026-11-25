package cn.hwjcode.straberryguild.ai.controller;

import cn.hwjcode.straberryguild.ai.dto.AiChatRequest;
import cn.hwjcode.straberryguild.ai.service.AiChatService;
import cn.hwjcode.straberryguild.common.results.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 草莓姬 AI 聊天接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@Valid @RequestBody AiChatRequest request) {
        try {
            String reply = aiChatService.chat(request);
            if (reply == null || reply.isBlank()) {
                return Result.fail(500, "草莓姬走神了，请再说一次～");
            }
            return Result.ok(Map.of("reply", reply));
        } catch (Exception e) {
            log.warn("AI 对话调用失败: {}", e.getMessage());
            return Result.fail(500, "AI 服务暂时不可用，请稍后再试");
        }
    }
}
