package com.smartforum.controller;

import com.smartforum.common.Result;
import com.smartforum.service.PostReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class PostReportController {

    @Autowired
    private PostReportService postReportService;

    @PostMapping("/post/{postId}")
    public Result<?> submitReport(@RequestAttribute("userId") Long userId,
                                  @PathVariable Long postId,
                                  @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        String description = body.get("description");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("请选择举报原因");
        }
        postReportService.submitReport(userId, postId, reason, description);
        return Result.success("举报已提交，感谢您的反馈");
    }

    @GetMapping("/check/{postId}")
    public Result<?> checkReported(@RequestAttribute("userId") Long userId,
                                   @PathVariable Long postId) {
        boolean reported = postReportService.hasReported(userId, postId);
        return Result.success(reported);
    }
}
