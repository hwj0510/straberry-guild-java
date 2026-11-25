package cn.hwjcode.straberryguild.feedback.service.impl;

import cn.hwjcode.straberryguild.feedback.mapper.FeedbackMapper;
import cn.hwjcode.straberryguild.feedback.service.FeedbackService;
import cn.hwjcode.straberryguild.pojo.dto.FeedbackCreateRequest;
import cn.hwjcode.straberryguild.pojo.entity.Feedback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public Long createFeedback(Long userId, FeedbackCreateRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(request.getType());
        feedback.setContent(request.getContent().trim());
        feedback.setContact(request.getContact() == null ? null : request.getContact().trim());
        feedback.setPageUrl(request.getPageUrl() == null ? "" : request.getPageUrl().trim());
        feedback.setStatus(0);
        feedbackMapper.insert(feedback);
        return feedback.getId();
    }
}
