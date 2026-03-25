package com.smartforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartforum.common.Result;
import com.smartforum.entity.Post;
import com.smartforum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 发布文章（需登录）
     */
    @PostMapping("/create")
    public Result<?> createPost(@RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");

        if (title == null || title.trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        Post post = postService.createPost(userId, title.trim(), content);
        return Result.success(post);
    }

    /**
     * 文章列表（分页，公开接口，支持排序：latest / hot）
     */
    @GetMapping("/list")
    public Result<?> getPostList(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        IPage<Post> result = postService.getPostList(page, size, sort);
        return Result.success(result);
    }

    /**
     * 文章详情（公开接口，可选认证 - 登录用户可获取点赞/收藏状态）
     */
    @GetMapping("/detail/{id}")
    public Result<?> getPostDetail(@PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        // 增加浏览量
        postService.incrementViewCount(id);
        Post post = postService.getPostDetail(id, userId);
        return Result.success(post);
    }

    /**
     * 搜索文章（公开接口）
     */
    @GetMapping("/search")
    public Result<?> searchPosts(@RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Post> result = postService.searchPosts(keyword, page, size);
        return Result.success(result);
    }

    /**
     * 热搜榜 Top10（公开接口）
     */
    @GetMapping("/hot-search")
    public Result<?> getHotSearch() {
        List<Map<String, Object>> list = postService.getHotSearchList();
        return Result.success(list);
    }

    /**
     * 编辑文章（需登录，仅作者本人）
     */
    @PutMapping("/update/{id}")
    public Result<?> updatePost(@PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");

        if (title == null || title.trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        Post post = postService.updatePost(id, userId, title.trim(), content);
        return Result.success(post);
    }

    /**
     * 删除文章（需登录，仅作者本人，逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deletePost(@PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        postService.deletePost(id, userId);
        return Result.success("删除成功");
    }
}

