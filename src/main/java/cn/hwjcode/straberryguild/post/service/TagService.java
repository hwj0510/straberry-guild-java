package cn.hwjcode.straberryguild.post.service;

import cn.hwjcode.straberryguild.pojo.dto.TagRequest;
import cn.hwjcode.straberryguild.pojo.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 创建标签
     */
    TagVO createTag(TagRequest request);

    /**
     * 更新标签
     */
    TagVO updateTag(Long tagId, TagRequest request);

    /**
     * 删除标签
     */
    void deleteTag(Long tagId);

    /**
     * 获取所有标签
     */
    List<TagVO> getAllTags();

    /**
     * 根据 ID 获取标签
     */
    TagVO getTagById(Long tagId);
}