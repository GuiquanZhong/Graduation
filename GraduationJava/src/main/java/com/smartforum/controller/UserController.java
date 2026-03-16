package com.smartforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartforum.common.Result;
import com.smartforum.entity.Comment;
import com.smartforum.entity.Post;
import com.smartforum.entity.User;
import com.smartforum.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostFavoriteService postFavoriteService;

    @Autowired
    private PostLikeService postLikeService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");

        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            return Result.error("用户名长度需在3-20之间");
        }
        if (password.length() < 6) {
            return Result.error("密码长度不能少于6位");
        }

        userService.register(username, password, nickname);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }

        Map<String, Object> data = userService.login(username, password);
        return Result.success(data);
    }

    /**
     * 获取当前用户信息（需登录）
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 获取他人主页资料（可选认证 - 登录用户可获取是否已关注状态）
     */
    @GetMapping("/profile/{targetUserId}")
    public Result<?> getUserProfile(@PathVariable Long targetUserId,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        User user = userService.getUserById(targetUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("username", user.getUsername());
        profile.put("followingCount", userFollowService.getFollowingCount(targetUserId));
        profile.put("followerCount", userFollowService.getFollowerCount(targetUserId));
        profile.put("isFollowed", userFollowService.isFollowing(currentUserId, targetUserId));

        return Result.success(profile);
    }

    /**
     * 关注/取消关注（需登录）
     */
    @PostMapping("/follow/{targetUserId}")
    public Result<?> toggleFollow(@RequestAttribute("userId") Long userId,
            @PathVariable Long targetUserId) {
        boolean followed = userFollowService.toggleFollow(userId, targetUserId);
        return Result.success(followed);
    }

    /**
     * 获取某用户发布的帖子（公开接口）
     */
    @GetMapping("/{userId}/posts")
    public Result<?> getUserPosts(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Post> result = postService.getPostsByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取某用户的回答/评论（公开接口）
     */
    @GetMapping("/{userId}/comments")
    public Result<?> getUserComments(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Comment> result = commentService.getCommentsByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取某用户的收藏（公开接口）
     */
    @GetMapping("/{userId}/favorites")
    public Result<?> getUserFavorites(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Post> result = postFavoriteService.getFavoritePostsByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取某用户的点赞帖子（公开接口）
     */
    @GetMapping("/{userId}/likes")
    public Result<?> getUserLikes(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Post> result = postLikeService.getLikedPostsByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取某用户的关注列表（公开接口）
     */
    @GetMapping("/{userId}/following")
    public Result<?> getUserFollowing(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Map<String, Object>> result = userFollowService.getFollowingList(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取某用户的粉丝列表（公开接口）
     */
    @GetMapping("/{userId}/followers")
    public Result<?> getUserFollowers(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Map<String, Object>> result = userFollowService.getFollowerList(userId, page, size);
        return Result.success(result);
    }

    /**
     * 删除收藏（需登录，只能删除自己的）
     */
    @DeleteMapping("/favorites/{postId}")
    public Result<?> removeFavorite(@RequestAttribute("userId") Long userId,
            @PathVariable Long postId) {
        postFavoriteService.removeFavorite(userId, postId);
        return Result.success("已取消收藏");
    }

    /**
     * 删除点赞（需登录，只能删除自己的）
     */
    @DeleteMapping("/likes/{postId}")
    public Result<?> removeLike(@RequestAttribute("userId") Long userId,
            @PathVariable Long postId) {
        postLikeService.removeLike(userId, postId);
        return Result.success("已取消点赞");
    }

    /**
     * 取消关注（需登录，只能取消自己的关注）
     */
    @DeleteMapping("/following/{targetUserId}")
    public Result<?> removeFollow(@RequestAttribute("userId") Long userId,
            @PathVariable Long targetUserId) {
        userFollowService.removeFollow(userId, targetUserId);
        return Result.success("已取消关注");
    }
}
