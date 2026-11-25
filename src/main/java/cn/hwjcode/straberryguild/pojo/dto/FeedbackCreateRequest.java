package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackCreateRequest {

    @NotBlank(message = "反馈类型不能为空")
    @Pattern(regexp = "BUG|FEATURE|OTHER", message = "反馈类型不正确")
    private String type;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 1000, message = "反馈内容不能超过 1000 字")
    private String content;

    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "联系邮箱不能超过 100 字")
    private String contact;

    @Size(max = 500, message = "页面路径不能超过 500 字")
    private String pageUrl;
}
