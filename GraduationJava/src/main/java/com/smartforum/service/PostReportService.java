package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartforum.entity.Post;
import com.smartforum.entity.PostReport;
import com.smartforum.entity.User;
import com.smartforum.mapper.PostMapper;
import com.smartforum.mapper.PostReportMapper;
import com.smartforum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostReportService {

    @Autowired
    private PostReportMapper postReportMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    public void submitReport(Long reporterId, Long postId, String reason, String description) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        LambdaQueryWrapper<PostReport> check = new LambdaQueryWrapper<>();
        check.eq(PostReport::getReporterId, reporterId)
             .eq(PostReport::getPostId, postId);
        if (postReportMapper.selectCount(check) > 0) {
            throw new RuntimeException("您已举报过该帖子，请勿重复举报");
        }
        PostReport report = new PostReport();
        report.setPostId(postId);
        report.setReporterId(reporterId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("pending");
        postReportMapper.insert(report);
    }

    public IPage<PostReport> getReportList(int page, int size, String status) {
        Page<PostReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PostReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            wrapper.eq(PostReport::getStatus, status);
        }
        wrapper.orderByAsc(PostReport::getStatus)
               .orderByDesc(PostReport::getCreatedAt);
        IPage<PostReport> result = postReportMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(r -> {
            User reporter = userMapper.selectById(r.getReporterId());
            if (reporter != null) {
                r.setReporterName(reporter.getNickname() != null ? reporter.getNickname() : reporter.getUsername());
                r.setReporterAvatar(reporter.getAvatar());
            }
            Post post = postMapper.selectById(r.getPostId());
            if (post != null) {
                r.setPostTitle(post.getTitle());
            } else {
                r.setPostTitle("[帖子已删除]");
            }
        });
        return result;
    }

    public void handleReport(Long reportId, String status, String handleNote) {
        PostReport report = postReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }
        report.setStatus(status);
        report.setHandleNote(handleNote);
        report.setHandledAt(LocalDateTime.now());
        postReportMapper.updateById(report);
    }

    public long getPendingCount() {
        LambdaQueryWrapper<PostReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostReport::getStatus, "pending");
        return postReportMapper.selectCount(wrapper);
    }

    public boolean hasReported(Long userId, Long postId) {
        LambdaQueryWrapper<PostReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostReport::getReporterId, userId)
               .eq(PostReport::getPostId, postId);
        return postReportMapper.selectCount(wrapper) > 0;
    }
}
