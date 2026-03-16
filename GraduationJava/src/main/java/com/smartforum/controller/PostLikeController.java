package com.smartforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartforum.common.Result;
import com.smartforum.entity.Post;
import com.smartforum.service.PostLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post/like")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    /**
     * 切换点赞状态（需登录）
     */
    @PostMapping("/{postId}")
    public Result<?> toggleLike(@RequestAttribute("userId") Long userId,
            @PathVariable Long postId) {
        boolean liked = postLikeService.toggleLike(userId, postId);
        return Result.success(liked);
    }

    /**
     * 获取当前用户点赞的帖子列表（需登录）
     */
    @GetMapping("/user")
    public Result<?> getUserLikedPosts(@RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Post> result = postLikeService.getUserLikedPosts(userId, page, size);
        return Result.success(result);
    }
}
