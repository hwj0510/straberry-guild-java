package cn.hwjcode.straberryguild.feedback.controller;

import cn.hwjcode.straberryguild.common.results.Result;
import cn.hwjcode.straberryguild.feedback.service.FeedbackService;
import cn.hwjcode.straberryguild.pojo.dto.FeedbackCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户反馈接口
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public Result<Long> createFeedback(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody FeedbackCreateRequest request) {
        return Result.ok(feedbackService.createFeedback(userId, request), "反馈提交成功");
    }
}
