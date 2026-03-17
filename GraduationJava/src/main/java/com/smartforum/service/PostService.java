package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartforum.entity.Post;
import com.smartforum.entity.PostLike;
import com.smartforum.entity.PostFavorite;
import com.smartforum.entity.User;
import com.smartforum.mapper.PostMapper;
import com.smartforum.mapper.PostLikeMapper;
import com.smartforum.mapper.PostFavoriteMapper;
import com.smartforum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostFavoriteMapper postFavoriteMapper;

    /**
     * 发布文章
     */
    public Post createPost(Long userId, String title, String content) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        postMapper.insert(post);
        return post;
    }

    /**
     * 分页获取文章列表
     */
    public IPage<Post> getPostList(int page, int size) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pageParam, wrapper);

        // 填充作者昵称
        result.getRecords().forEach(this::fillAuthorName);
        return result;
    }

    /**
     * 搜索文章（按标题和内容模糊匹配）
     */
    public IPage<Post> searchPosts(String keyword, int page, int size) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Post::getTitle, keyword)
                .or()
                .like(Post::getContent, keyword);
        wrapper.orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pageParam, wrapper);

        result.getRecords().forEach(this::fillAuthorName);
        return result;
    }

    /**
     * 获取文章详情（支持可选的 userId 用于查询当前用户点赞/收藏状态）
     */
    public Post getPostDetail(Long id, Long userId) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }
        fillAuthorName(post);
        fillInteractionInfo(post, userId);
        return post;
    }

    /**
     * 获取文章详情（无用户上下文）
     */
    public Post getPostDetail(Long id) {
        return getPostDetail(id, null);
    }

    /**
     * 更新文章摘要
     */
    public void updateSummary(Long postId, String summary) {
        Post post = new Post();
        post.setId(postId);
        post.setSummary(summary);
        postMapper.updateById(post);
    }

    /**
     * 编辑文章（仅作者本人）
     */
    public Post updatePost(Long postId, Long userId, String title, String content) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此文章");
        }
        post.setTitle(title);
        post.setContent(content);
        postMapper.updateById(post);
        return post;
    }

    /**
     * 删除文章（仅作者本人，逻辑删除）
     */
    public void deletePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此文章");
        }
        postMapper.deleteById(postId);
    }

    /**
     * 填充作者昵称
     */
    private void fillAuthorName(Post post) {
        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            post.setAuthorName(author.getNickname());
            post.setAuthorAvatar(author.getAvatar());
        }
    }

    /**
     * 分页获取某用户发布的文章
     */
    public IPage<Post> getPostsByUserId(Long userId, int page, int size) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillAuthorName);
        return result;
    }

    /**
     * 填充帖子的点赞数、收藏数及当前用户的交互状态
     */
    public void fillInteractionInfo(Post post, Long userId) {
        // 点赞数
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getPostId, post.getId());
        post.setLikeCount(postLikeMapper.selectCount(likeWrapper));

        // 收藏数
        LambdaQueryWrapper<PostFavorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(PostFavorite::getPostId, post.getId());
        post.setFavoriteCount(postFavoriteMapper.selectCount(favWrapper));

        // 当前用户是否已点赞/收藏
        if (userId != null) {
            LambdaQueryWrapper<PostLike> userLikeWrapper = new LambdaQueryWrapper<>();
            userLikeWrapper.eq(PostLike::getPostId, post.getId())
                    .eq(PostLike::getUserId, userId);
            post.setIsLiked(postLikeMapper.selectCount(userLikeWrapper) > 0);

            LambdaQueryWrapper<PostFavorite> userFavWrapper = new LambdaQueryWrapper<>();
            userFavWrapper.eq(PostFavorite::getPostId, post.getId())
                    .eq(PostFavorite::getUserId, userId);
            post.setIsFavorited(postFavoriteMapper.selectCount(userFavWrapper) > 0);
        } else {
            post.setIsLiked(false);
            post.setIsFavorited(false);
        }
    }
}
