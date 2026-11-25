package cn.hwjcode.straberryguild.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论展示对象
 */
@Data
@Builder
public class CommentVO {

    private Long id;
    private Long postId;
    private String content;
    private LocalDateTime createTime;
    private PostListVO.AuthorVO author;
}
