package cn.hwjcode.straberryguild.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情 VO
 */
@Data
@Builder
public class PostDetailVO {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String cover;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer shareCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private PostListVO.AuthorVO author;

    private List<TagVO> tags;
}