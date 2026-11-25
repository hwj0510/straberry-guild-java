package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 忘记密码 - 重置密码请求（账号 + 注册邮箱匹配校验通过后，提交新密码）
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度 6-100")
    private String newPassword;
}
