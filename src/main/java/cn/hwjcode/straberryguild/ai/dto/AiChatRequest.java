package cn.hwjcode.straberryguild.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * AI 聊天请求
 */
@Data
public class AiChatRequest {

    /** 人格 ID：healing / genki / tsundere / otaku */
    private String persona = "healing";

    /** 用户昵称，用于个性化称呼 */
    @Size(max = 32, message = "昵称过长")
    private String nickname;

    /** 对话历史（含本次用户消息） */
    @NotEmpty(message = "消息不能为空")
    private List<MessageItem> messages;

    @Data
    public static class MessageItem {
        /** user / ai */
        @NotBlank
        private String role;
        @NotBlank
        private String content;
    }
}
