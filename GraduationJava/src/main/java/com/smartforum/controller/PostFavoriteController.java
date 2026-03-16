package com.smartforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartforum.common.Result;
import com.smartforum.entity.Post;
import com.smartforum.service.PostFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post/favorite")
public class PostFavoriteController {

    @Autowired
    private PostFavoriteService postFavoriteService;

    /**
     * 切换收藏状态（需登录）
     */
    @PostMapping("/{postId}")
    public Result<?> toggleFavorite(@RequestAttribute("userId") Long userId,
            @PathVariable Long postId) {
        boolean favorited = postFavoriteService.toggleFavorite(userId, postId);
        return Result.success(favorited);
    }

    /**
     * 获取当前用户收藏的帖子列表（需登录）
     */
    @GetMapping("/user")
    public Result<?> getUserFavoritePosts(@RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Post> result = postFavoriteService.getUserFavoritePosts(userId, page, size);
        return Result.success(result);
    }
}
