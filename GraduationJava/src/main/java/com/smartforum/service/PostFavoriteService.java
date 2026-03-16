package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartforum.entity.Post;
import com.smartforum.entity.PostFavorite;
import com.smartforum.entity.User;
import com.smartforum.mapper.PostFavoriteMapper;
import com.smartforum.mapper.PostMapper;
import com.smartforum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostFavoriteService {

    @Autowired
    private PostFavoriteMapper postFavoriteMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 切换收藏状态（收藏/取消收藏）
     * 
     * @return true=已收藏，false=已取消
     */
    public boolean toggleFavorite(Long userId, Long postId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId);
        PostFavorite existing = postFavoriteMapper.selectOne(wrapper);

        if (existing != null) {
            postFavoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            PostFavorite favorite = new PostFavorite();
            favorite.setUserId(userId);
            favorite.setPostId(postId);
            postFavoriteMapper.insert(favorite);
            return true;
        }
    }

    /**
     * 获取帖子收藏数
     */
    public long getFavoriteCount(Long postId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostFavorite::getPostId, postId);
        return postFavoriteMapper.selectCount(wrapper);
    }

    /**
     * 判断用户是否已收藏
     */
    public boolean isFavorited(Long userId, Long postId) {
        if (userId == null)
            return false;
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId);
        return postFavoriteMapper.selectCount(wrapper) > 0;
    }

    /**
     * 分页获取用户已收藏的帖子
     */
    public IPage<Post> getUserFavoritePosts(Long userId, int page, int size) {
        // 先查出用户收藏的 postId 列表（按时间倒序）
        LambdaQueryWrapper<PostFavorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(PostFavorite::getUserId, userId)
                .orderByDesc(PostFavorite::getCreatedAt);
        List<PostFavorite> favorites = postFavoriteMapper.selectList(favWrapper);

        List<Long> postIds = favorites.stream()
                .map(PostFavorite::getPostId)
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
     * 分页获取指定用户的收藏帖子（公开接口）
     */
    public IPage<Post> getFavoritePostsByUserId(Long userId, int page, int size) {
        LambdaQueryWrapper<PostFavorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(PostFavorite::getUserId, userId)
                .orderByDesc(PostFavorite::getCreatedAt);
        List<PostFavorite> favorites = postFavoriteMapper.selectList(favWrapper);

        List<Long> postIds = favorites.stream()
                .map(PostFavorite::getPostId)
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
     * 取消收藏（从列表中删除）
     */
    public void removeFavorite(Long userId, Long postId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId);
        postFavoriteMapper.delete(wrapper);
    }
}
