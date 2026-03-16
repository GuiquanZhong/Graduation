package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartforum.entity.Post;
import com.smartforum.entity.PostLike;
import com.smartforum.entity.User;
import com.smartforum.mapper.PostLikeMapper;
import com.smartforum.mapper.PostMapper;
import com.smartforum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostLikeService {

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 切换点赞状态（点赞/取消点赞）
     * 
     * @return true=已点赞，false=已取消
     */
    public boolean toggleLike(Long userId, Long postId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId);
        PostLike existing = postLikeMapper.selectOne(wrapper);

        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            return false;
        } else {
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            postLikeMapper.insert(like);
            return true;
        }
    }

    /**
     * 获取帖子点赞数
     */
    public long getLikeCount(Long postId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId);
        return postLikeMapper.selectCount(wrapper);
    }

    /**
     * 判断用户是否已点赞
     */
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null)
            return false;
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId);
        return postLikeMapper.selectCount(wrapper) > 0;
    }

    /**
     * 分页获取用户已点赞的帖子
     */
    public IPage<Post> getUserLikedPosts(Long userId, int page, int size) {
        // 先查出用户点赞的 postId 列表（按时间倒序）
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getUserId, userId)
                .orderByDesc(PostLike::getCreatedAt);
        List<PostLike> likes = postLikeMapper.selectList(likeWrapper);

        List<Long> postIds = likes.stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return new Page<>(page, size);
        }

        // 分页查询帖子
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.in(Post::getId, postIds)
                .orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pageParam, postWrapper);

        // 填充作者昵称
        result.getRecords().forEach(post -> {
            User author = userMapper.selectById(post.getUserId());
            if (author != null) {
                post.setAuthorName(author.getNickname());
            }
        });

        return result;
    }

    /**
     * 分页获取指定用户的点赞帖子（公开接口）
     */
    public IPage<Post> getLikedPostsByUserId(Long userId, int page, int size) {
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getUserId, userId)
                .orderByDesc(PostLike::getCreatedAt);
        List<PostLike> likes = postLikeMapper.selectList(likeWrapper);

        List<Long> postIds = likes.stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return new Page<>(page, size);
        }

        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.in(Post::getId, postIds)
                .orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pageParam, postWrapper);

        result.getRecords().forEach(post -> {
            User author = userMapper.selectById(post.getUserId());
            if (author != null) {
                post.setAuthorName(author.getNickname());
                post.setAuthorAvatar(author.getAvatar());
            }
        });

        return result;
    }

    /**
     * 取消点赞（从列表中删除）
     */
    public void removeLike(Long userId, Long postId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId);
        postLikeMapper.delete(wrapper);
    }
}
