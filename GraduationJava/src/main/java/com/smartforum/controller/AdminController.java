package com.smartforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartforum.common.Result;
import com.smartforum.entity.Post;
import com.smartforum.entity.PostReport;
import com.smartforum.entity.User;
import com.smartforum.service.PostReportService;
import com.smartforum.service.PostService;
import com.smartforum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostReportService postReportService;

    private void checkAdmin(Long userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("无管理员权限");
        }
    }

    // ============ 数据统计 ============

    @GetMapping("/stats")
    public Result<?> getStats(@RequestAttribute("userId") Long userId) {
        checkAdmin(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userService.getUserCount());
        stats.put("postCount", postService.getPostCount());
        stats.put("pendingReportCount", postReportService.getPendingCount());
        stats.put("date", LocalDate.now().toString());
        return Result.success(stats);
    }

    // ============ 用户管理 ============

    @GetMapping("/users")
    public Result<?> getUserList(@RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        checkAdmin(userId);
        IPage<User> result = userService.getAllUsers(page, size, keyword);
        return Result.success(result);
    }

    @PutMapping("/user/{targetId}/ban")
    public Result<?> banUser(@RequestAttribute("userId") Long userId,
            @PathVariable Long targetId) {
        checkAdmin(userId);
        userService.banUser(targetId);
        return Result.success("封禁成功");
    }

    @PutMapping("/user/{targetId}/unban")
    public Result<?> unbanUser(@RequestAttribute("userId") Long userId,
            @PathVariable Long targetId) {
        checkAdmin(userId);
        userService.unbanUser(targetId);
        return Result.success("解封成功");
    }

    @PutMapping("/user/{targetId}/role")
    public Result<?> updateUserRole(@RequestAttribute("userId") Long userId,
            @PathVariable Long targetId,
            @RequestParam String role) {
        checkAdmin(userId);
        if (userId.equals(targetId)) {
            return Result.error("不能修改自己的角色");
        }
        userService.updateRole(targetId, role);
        return Result.success("角色已更新");
    }

    // ============ 帖子管理 ============

    @GetMapping("/posts")
    public Result<?> getPostList(@RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        checkAdmin(userId);
        IPage<Post> result = postService.getAllPosts(page, size, keyword);
        return Result.success(result);
    }

    @DeleteMapping("/post/{postId}")
    public Result<?> deletePost(@RequestAttribute("userId") Long userId,
            @PathVariable Long postId) {
        checkAdmin(userId);
        postService.adminDeletePost(postId);
        return Result.success("删除成功");
    }

    @PutMapping("/post/{postId}/top")
    public Result<?> toggleTopPost(@RequestAttribute("userId") Long userId,
            @PathVariable Long postId) {
        checkAdmin(userId);
        boolean isTop = postService.toggleTopPost(postId);
        return Result.success(isTop ? "已置顶" : "已取消置顶");
    }

    // ============ 举报管理 ============

    @GetMapping("/reports")
    public Result<?> getReportList(@RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String status) {
        checkAdmin(userId);
        IPage<PostReport> result = postReportService.getReportList(page, size, status);
        return Result.success(result);
    }

    @PutMapping("/report/{reportId}/handle")
    public Result<?> handleReport(@RequestAttribute("userId") Long userId,
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body) {
        checkAdmin(userId);
        String status = body.get("status");
        String handleNote = body.get("handleNote");
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            return Result.error("无效的处理状态");
        }
        postReportService.handleReport(reportId, status, handleNote);
        return Result.success("处理成功");
    }
}
