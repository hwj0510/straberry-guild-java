package cn.hwjcode.straberryguild.feedback.service;

import cn.hwjcode.straberryguild.pojo.dto.FeedbackCreateRequest;

public interface FeedbackService {

    Long createFeedback(Long userId, FeedbackCreateRequest request);
}
