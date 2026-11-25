package cn.hwjcode.straberryguild.pojo.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新帖子请求
 */
@Data
public class PostUpdateRequest {

    @Size(max = 200, message = "标题长度不能超过 200 个字符")
    private String title;

    private String content;

    private String category;

    private String cover;

    private List<Long> tagIds;
}