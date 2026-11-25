package cn.hwjcode.straberryguild.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表项 VO
 */
@Data
@Builder
public class PostListVO {

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
    private Boolean liked;
    private Boolean collected;
    private LocalDateTime createTime;

    private AuthorVO author;

    private List<TagVO> tags;

    @Data
    @Builder
    public static class AuthorVO {
        private Long id;
        private String nickname;
        private String avatar;
        /** 当前登录用户是否已关注该作者 */
        private Boolean following;
    }
}