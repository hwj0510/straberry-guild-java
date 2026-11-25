package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码 - 身份校验请求（账号 + 注册邮箱匹配）
 */
@Data
public class ForgotPasswordVerifyRequest {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
