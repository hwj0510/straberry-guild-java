package cn.hwjcode.straberryguild.post.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hwjcode.straberryguild.common.exception.BusinessException;
import cn.hwjcode.straberryguild.pojo.dto.TagRequest;
import cn.hwjcode.straberryguild.pojo.vo.TagVO;
import cn.hwjcode.straberryguild.pojo.entity.PostTag;
import cn.hwjcode.straberryguild.pojo.entity.Tag;
import cn.hwjcode.straberryguild.post.mapper.PostTagMapper;
import cn.hwjcode.straberryguild.post.mapper.TagMapper;
import cn.hwjcode.straberryguild.post.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;

    @Override
    @Transactional
    public TagVO createTag(TagRequest request) {
        Long count = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>().eq(Tag::getName, request.getName())
        );
        if (count > 0) {
            throw BusinessException.badRequest("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setPostCount(0);
        tagMapper.insert(tag);

        log.info("标签创建成功: tagId={}, name={}", tag.getId(), tag.getName());
        return TagVO.fromEntity(tag);
    }

    @Override
    @Transactional
    public TagVO updateTag(Long tagId, TagRequest request) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw BusinessException.notFound("标签不存在");
        }

        if (StrUtil.isNotBlank(request.getName()) && !request.getName().equals(tag.getName())) {
            Long count = tagMapper.selectCount(
                    new LambdaQueryWrapper<Tag>().eq(Tag::getName, request.getName()).ne(Tag::getId, tagId)
            );
            if (count > 0) {
                throw BusinessException.badRequest("标签名称已存在");
            }
            tag.setName(request.getName());
            tagMapper.updateById(tag);
        }

        return TagVO.fromEntity(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw BusinessException.notFound("标签不存在");
        }

        postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tagId));
        tagMapper.deleteById(tagId);

        log.info("标签删除成功: tagId={}", tagId);
    }

    @Override
    public List<TagVO> getAllTags() {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getPostCount)
        );
        return tags.stream().map(TagVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public TagVO getTagById(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw BusinessException.notFound("标签不存在");
        }
        return TagVO.fromEntity(tag);
    }
}