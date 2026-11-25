package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料请求 DTO
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称长度不能超过 30")
    private String nickname;

    @Size(max = 500, message = "简介长度不能超过 500")
    private String bio;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    private String avatar;

    /** 0-未知 1-男 2-女 */
    private Integer gender;
}