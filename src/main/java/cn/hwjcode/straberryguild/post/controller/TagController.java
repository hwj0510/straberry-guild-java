package cn.hwjcode.straberryguild.post.controller;

import cn.hwjcode.straberryguild.common.results.Result;
import cn.hwjcode.straberryguild.pojo.dto.TagRequest;
import cn.hwjcode.straberryguild.pojo.vo.TagVO;
import cn.hwjcode.straberryguild.post.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签接口控制器
 * 基础路径: /api/tags
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 创建标签
     * POST /api/tags
     */
    @PostMapping
    public Result<TagVO> createTag(@Valid @RequestBody TagRequest request) {
        return Result.ok(tagService.createTag(request), "创建成功");
    }

    /**
     * 更新标签
     * PUT /api/tags/{tagId}
     */
    @PutMapping("/{tagId}")
    public Result<TagVO> updateTag(@PathVariable Long tagId,
                                   @Valid @RequestBody TagRequest request) {
        return Result.ok(tagService.updateTag(tagId, request), "更新成功");
    }

    /**
     * 删除标签
     * DELETE /api/tags/{tagId}
     */
    @DeleteMapping("/{tagId}")
    public Result<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return Result.ok(null, "删除成功");
    }

    /**
     * 获取所有标签
     * GET /api/tags
     */
    @GetMapping
    public Result<List<TagVO>> getAllTags() {
        return Result.ok(tagService.getAllTags());
    }

    /**
     * 根据 ID 获取标签
     * GET /api/tags/{tagId}
     */
    @GetMapping("/{tagId}")
    public Result<TagVO> getTagById(@PathVariable Long tagId) {
        return Result.ok(tagService.getTagById(tagId));
    }
}