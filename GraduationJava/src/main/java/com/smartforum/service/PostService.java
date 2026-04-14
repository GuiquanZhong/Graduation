package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartforum.entity.*;
import com.smartforum.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostReportMapper postReportMapper;

    /**
     * 发布文章
     */
    public Post createPost(Long userId, String title, String content) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setIsTop(0);
        post.setViewCount(0);
        post.setDailyViewCount(0);
        postMapper.insert(post);
        return post;
    }

    /**
     * 分页获取文章列表（支持排序方式：latest-最新，hot-最热）
     */
    public IPage<Post> getPostList(int page, int size, String sort) {
        if ("hot".equals(sort)) {
            return getHotPostList(page, size);
        }
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pageParam, wrapper);

        // 填充作者昵称和交互数据
        result.getRecords().forEach(p -> {
            fillAuthorName(p);
            fillInteractionCounts(p);
        });
        return result;
    }

    /**
     * 分页获取文章列表（默认最新，兼容旧调用）
     */
    public IPage<Post> getPostList(int page, int size) {
        return getPostList(page, size, "latest");
    }

    /**
     * 热度排序：按加权分数排序 (点赞*3 + 评论*2 + 浏览*0.5 + 收藏*4)
     */
    private IPage<Post> getHotPostList(int page, int size) {
        // 查出所有帖子（合理范围内，取 Top 200 参与排序）
        Page<Post> bigPage = new Page<>(1, 200);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Post::getCreatedAt);
        IPage<Post> allPosts = postMapper.selectPage(bigPage, wrapper);

        allPosts.getRecords().forEach(p -> {
            fillAuthorName(p);
            fillInteractionCounts(p);
            // 计算热度分
            double score = (p.getLikeCount() != null ? p.getLikeCount() : 0) * 3
                    + (p.getCommentCount() != null ? p.getCommentCount() : 0) * 2
                    + (p.getViewCount() != null ? p.getViewCount() : 0) * 0.5
                    + (p.getFavoriteCount() != null ? p.getFavoriteCount() : 0) * 4;
            // 置顶帖加巨额分数
            if (p.getIsTop() != null && p.getIsTop() == 1) {
                score += 1000000;
            }
            p.setHotScore(score);
        });

        // 按热度降序排列
        List<Post> sorted = allPosts.getRecords().stream()
                .sorted(Comparator.comparingDouble(Post::getHotScore).reversed())
                .collect(Collectors.toList());

        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, sorted.size());
        List<Post> pageList = start < sorted.size() ? sorted.subList(start, end) : new ArrayList<>();

        Page<Post> resultPage = new Page<>(page, size, sorted.size());
        resultPage.setRecords(pageList);
        return resultPage;
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

        result.getRecords().forEach(p -> {
            fillAuthorName(p);
            fillInteractionCounts(p);
        });
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
     * 增加浏览量
     */
    public void incrementViewCount(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            Post update = new Post();
            update.setId(postId);
            update.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
            update.setDailyViewCount((post.getDailyViewCount() != null ? post.getDailyViewCount() : 0) + 1);
            postMapper.updateById(update);
        }
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
        deletePostCascade(postId);
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
     * 仅填充点赞数、评论数、收藏数（列表用，不查用户交互状态）
     */
    private void fillInteractionCounts(Post post) {
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getPostId, post.getId());
        post.setLikeCount(postLikeMapper.selectCount(likeWrapper));

        LambdaQueryWrapper<PostFavorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(PostFavorite::getPostId, post.getId());
        post.setFavoriteCount(postFavoriteMapper.selectCount(favWrapper));

        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getPostId, post.getId());
        post.setCommentCount(commentMapper.selectCount(commentWrapper));
    }

    /**
     * 填充帖子的点赞数、收藏数、评论数及当前用户的交互状态
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

        // 评论数
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getPostId, post.getId());
        post.setCommentCount(commentMapper.selectCount(commentWrapper));

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

    // ============ 管理员功能 ============

    /**
     * 管理员删除文章（逻辑删除，无需验证作者）
     */
    public void adminDeletePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }
        deletePostCascade(postId);
    }

    private void deletePostCascade(Long postId) {
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, postId));
        postFavoriteMapper.delete(new LambdaQueryWrapper<PostFavorite>().eq(PostFavorite::getPostId, postId));
        postReportMapper.delete(new LambdaQueryWrapper<PostReport>().eq(PostReport::getPostId, postId));
        postMapper.deleteById(postId);
    }

    /**
     * 管理员置顶/取消置顶文章
     */
    public boolean toggleTopPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }
        int newTop = (post.getIsTop() != null && post.getIsTop() == 1) ? 0 : 1;
        Post update = new Post();
        update.setId(postId);
        update.setIsTop(newTop);
        postMapper.updateById(update);
        return newTop == 1;
    }

    /**
     * 获取所有文章（管理员用，含所有状态信息）
     */
    public IPage<Post> getAllPosts(int page, int size, String keyword) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Post::getTitle, keyword);
        }
        wrapper.orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(p -> {
            fillAuthorName(p);
            fillInteractionCounts(p);
        });
        return result;
    }

    /**
     * 获取文章总数（管理员用）
     */
    public long getPostCount() {
        return postMapper.selectCount(null);
    }

    /**
     * 获取热搜榜 Top10（按热度排序，返回简化信息）
     */
    public List<Map<String, Object>> getHotSearchList() {
        // 查最近的帖子
        Page<Post> page = new Page<>(1, 100);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Post::getCreatedAt);
        IPage<Post> allPosts = postMapper.selectPage(page, wrapper);

        allPosts.getRecords().forEach(this::fillInteractionCounts);

        // 计算热度并排序
        return allPosts.getRecords().stream()
                .map(p -> {
                    double score = (p.getLikeCount() != null ? p.getLikeCount() : 0) * 3
                            + (p.getCommentCount() != null ? p.getCommentCount() : 0) * 2
                            + (p.getViewCount() != null ? p.getViewCount() : 0) * 0.5
                            + (p.getFavoriteCount() != null ? p.getFavoriteCount() : 0) * 4;
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", p.getId());
                    item.put("title", p.getTitle());
                    item.put("summary", p.getSummary());
                    item.put("hotScore", score);
                    item.put("viewCount", p.getViewCount());
                    item.put("likeCount", p.getLikeCount());
                    item.put("commentCount", p.getCommentCount());
                    return item;
                })
                .sorted((a, b) -> Double.compare((double) b.get("hotScore"), (double) a.get("hotScore")))
                .limit(10)
                .collect(Collectors.toList());
    }
}

