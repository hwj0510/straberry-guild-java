package cn.hwjcode.straberryguild.post.controller;

import cn.hwjcode.straberryguild.common.results.Result;
import cn.hwjcode.straberryguild.pojo.dto.CommentCreateRequest;
import cn.hwjcode.straberryguild.pojo.vo.CommentVO;
import cn.hwjcode.straberryguild.pojo.dto.PostCreateRequest;
import cn.hwjcode.straberryguild.pojo.vo.PostDetailVO;
import cn.hwjcode.straberryguild.pojo.vo.PostListVO;
import cn.hwjcode.straberryguild.pojo.dto.PostUpdateRequest;
import cn.hwjcode.straberryguild.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 帖子接口控制器
 * 基础路径: /api/posts
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Result<PostDetailVO> createPost(@AuthenticationPrincipal Long userId,
                                            @Valid @RequestBody PostCreateRequest request) {
        return Result.ok(postService.createPost(userId, request), "创建成功");
    }

    @PutMapping("/{postId}")
    public Result<PostDetailVO> updatePost(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long postId,
                                            @Valid @RequestBody PostUpdateRequest request) {
        return Result.ok(postService.updatePost(userId, postId, request), "更新成功");
    }

    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@AuthenticationPrincipal Long userId,
                                   @PathVariable Long postId) {
        postService.deletePost(userId, postId);
        return Result.ok(null, "删除成功");
    }

    /** 获取帖子详情，同时累计一次浏览量 */
    @GetMapping("/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        return Result.ok(postService.getPostDetail(postId));
    }

    @GetMapping
    public Result<List<PostListVO>> getPostList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String feed) {
        return Result.ok(postService.getPostList(userId, page, size, tagId, keyword, category, feed, sort));
    }

    /** 统计帖子数量，可按分类筛选 */
    @GetMapping("/count")
    public Result<Map<String, Object>> countPosts(@RequestParam(required = false) String category) {
        return Result.ok(Map.of("count", postService.countPosts(category)));
    }

    @GetMapping("/mine")
    public Result<List<PostListVO>> getMyPosts(@AuthenticationPrincipal Long userId,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(postService.getUserPosts(userId, page, size));
    }

    /** 获取指定用户公开发布的帖子（可游客访问，登录后附带点赞/收藏/关注态） */
    @GetMapping("/user/{userId}")
    public Result<List<PostListVO>> getUserPublicPosts(@AuthenticationPrincipal Long viewerId,
                                                       @PathVariable Long userId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(postService.getUserPublicPosts(userId, viewerId, page, size));
    }

    /** 点赞/取消点赞 */
    @PostMapping("/{postId}/like")
    public Result<Map<String, Object>> toggleLike(@AuthenticationPrincipal Long userId,
                                                  @PathVariable Long postId) {
        return Result.ok(postService.toggleLike(userId, postId));
    }

    /** 收藏/取消收藏 */
    @PostMapping("/{postId}/collect")
    public Result<Map<String, Object>> toggleCollect(@AuthenticationPrincipal Long userId,
                                                     @PathVariable Long postId) {
        return Result.ok(postService.toggleCollect(userId, postId));
    }

    @GetMapping("/{postId}/comments")
    public Result<List<CommentVO>> getComments(@PathVariable Long postId) {
        return Result.ok(postService.getComments(postId));
    }

    @PostMapping("/{postId}/comments")
    public Result<CommentVO> createComment(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long postId,
                                           @Valid @RequestBody CommentCreateRequest request) {
        return Result.ok(postService.createComment(userId, postId, request), "评论成功");
    }
}
