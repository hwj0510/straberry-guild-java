package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建帖子请求
 */
@Data
public class PostCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200 个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /** 分类，默认"推荐" */
    private String category;

    private String cover;

    private List<Long> tagIds;

    /** 标签名称列表，后端会自动创建不存在的标签 */
    private List<String> tags;
}