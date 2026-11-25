package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {

    /** 用户名 / 邮箱 / 手机号 */
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 50, message = "账号长度 3-50")
    private String account;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度 6-100")
    private String password;
}
