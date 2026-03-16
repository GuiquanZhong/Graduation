package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartforum.entity.Comment;
import com.smartforum.entity.Post;
import com.smartforum.entity.User;
import com.smartforum.mapper.CommentMapper;
import com.smartforum.mapper.PostMapper;
import com.smartforum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 发表评论
     */
    public Comment addComment(Long postId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        commentMapper.insert(comment);
        return comment;
    }

    /**
     * 获取文章的所有评论
     */
    public List<Comment> getCommentsByPostId(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .orderByAsc(Comment::getCreatedAt);
        List<Comment> comments = commentMapper.selectList(wrapper);

        // 填充评论者昵称
        comments.forEach(comment -> {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setAuthorName(user.getNickname());
            }
        });

        return comments;
    }

    /**
     * 分页获取某用户的所有评论（回答）
     */
    public IPage<Comment> getCommentsByUserId(Long userId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreatedAt);
        IPage<Comment> result = commentMapper.selectPage(pageParam, wrapper);

        // 填充评论者昵称和关联帖子标题
        result.getRecords().forEach(comment -> {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setAuthorName(user.getNickname());
            }
            // 填充帖子标题
            Post post = postMapper.selectById(comment.getPostId());
            if (post != null) {
                comment.setPostTitle(post.getTitle());
            }
        });

        return result;
    }

    /**
     * 删除评论（仅评论者本人）
     */
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该评论");
        }
        commentMapper.deleteById(commentId);
    }
}
