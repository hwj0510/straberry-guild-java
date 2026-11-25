package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求
 */
@Data
public class CommentCreateRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论长度不能超过 500 个字符")
    private String content;
}
