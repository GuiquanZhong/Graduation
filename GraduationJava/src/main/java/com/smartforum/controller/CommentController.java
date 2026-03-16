package com.smartforum.controller;

import com.smartforum.common.Result;
import com.smartforum.entity.Comment;
import com.smartforum.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论（需登录）
     */
    @PostMapping("/add")
    public Result<?> addComment(@RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> params) {
        Long postId = Long.valueOf(params.get("postId").toString());
        String content = (String) params.get("content");

        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        Comment comment = commentService.addComment(postId, userId, content.trim());
        return Result.success(comment);
    }

    /**
     * 获取文章评论列表（公开接口由拦截器控制，这里按需放行）
     */
    @GetMapping("/list/{postId}")
    public Result<?> getComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return Result.success(comments);
    }

    /**
     * 删除评论（需登录，仅本人可删）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteComment(@PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        commentService.deleteComment(id, userId);
        return Result.success("删除成功");
    }
}
