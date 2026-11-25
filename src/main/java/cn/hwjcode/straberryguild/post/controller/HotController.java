package cn.hwjcode.straberryguild.post.controller;

import cn.hwjcode.straberryguild.common.results.Result;
import cn.hwjcode.straberryguild.pojo.vo.PostListVO;
import cn.hwjcode.straberryguild.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotController {

    private final PostService postService;

    /**
     * 获取热门帖子
     * GET /api/hot
     */
    @GetMapping("/hot")
    public Result<List<PostListVO>> getHotPosts(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String category) {
        return Result.ok(postService.getHotPosts(limit, category));
    }
}