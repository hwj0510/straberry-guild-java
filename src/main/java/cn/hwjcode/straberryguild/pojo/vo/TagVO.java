package cn.hwjcode.straberryguild.pojo.vo;

import cn.hwjcode.straberryguild.pojo.entity.Tag;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签响应 VO
 */
@Data
@Builder
public class TagVO {

    private Long id;
    private String name;
    private Integer postCount;
    private LocalDateTime createTime;

    public static TagVO fromEntity(Tag tag) {
        if (tag == null) return null;
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .postCount(tag.getPostCount())
                .createTime(tag.getCreateTime())
                .build();
    }
}