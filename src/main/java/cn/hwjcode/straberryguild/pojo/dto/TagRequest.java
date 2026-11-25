package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建/更新标签请求
 */
@Data
public class TagRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 30, message = "标签名称长度不能超过 30 个字符")
    private String name;
}