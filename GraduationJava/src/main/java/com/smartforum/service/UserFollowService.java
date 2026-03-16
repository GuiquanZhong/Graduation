package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartforum.entity.User;
import com.smartforum.entity.UserFollow;
import com.smartforum.mapper.UserFollowMapper;
import com.smartforum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 关注/取消关注
     * 
     * @return true=已关注，false=已取消
     */
    public boolean toggleFollow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new RuntimeException("不能关注自己");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowedId, followedId);
        UserFollow existing = userFollowMapper.selectOne(wrapper);

        if (existing != null) {
            userFollowMapper.deleteById(existing.getId());
            return false;
        } else {
            UserFollow follow = new UserFollow();
            follow.setFollowerId(followerId);
            follow.setFollowedId(followedId);
            userFollowMapper.insert(follow);
            return true;
        }
    }

    /**
     * 获取用户关注数（他关注了多少人）
     */
    public long getFollowingCount(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId);
        return userFollowMapper.selectCount(wrapper);
    }

    /**
     * 获取用户粉丝数（多少人关注了他）
     */
    public long getFollowerCount(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowedId, userId);
        return userFollowMapper.selectCount(wrapper);
    }

    /**
     * 判断 followerId 是否已关注 followedId
     */
    public boolean isFollowing(Long followerId, Long followedId) {
        if (followerId == null)
            return false;
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowedId, followedId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    /**
     * 分页获取用户的关注列表（他关注了哪些人）
     */
    public IPage<Map<String, Object>> getFollowingList(Long userId, int page, int size) {
        // 先查出关注关系
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
                .orderByDesc(UserFollow::getCreatedAt);
        List<UserFollow> follows = userFollowMapper.selectList(wrapper);

        List<Long> userIds = follows.stream()
                .map(UserFollow::getFollowedId)
                .collect(Collectors.toList());

        return buildUserPage(userIds, page, size);
    }

    /**
     * 分页获取用户的粉丝列表（哪些人关注了他）
     */
    public IPage<Map<String, Object>> getFollowerList(Long userId, int page, int size) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowedId, userId)
                .orderByDesc(UserFollow::getCreatedAt);
        List<UserFollow> follows = userFollowMapper.selectList(wrapper);

        List<Long> userIds = follows.stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());

        return buildUserPage(userIds, page, size);
    }

    /**
     * 取消关注（从列表中删除）
     */
    public void removeFollow(Long followerId, Long followedId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowedId, followedId);
        userFollowMapper.delete(wrapper);
    }

    /**
     * 构建用户分页数据
     */
    private IPage<Map<String, Object>> buildUserPage(List<Long> userIds, int page, int size) {
        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        if (userIds.isEmpty()) {
            resultPage.setRecords(List.of());
            resultPage.setTotal(0);
            return resultPage;
        }

        // 手动分页
        int total = userIds.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        if (start >= total) {
            resultPage.setRecords(List.of());
            resultPage.setTotal(total);
            return resultPage;
        }
        List<Long> pageIds = userIds.subList(start, end);

        List<Map<String, Object>> records = pageIds.stream().map(uid -> {
            User user = userMapper.selectById(uid);
            Map<String, Object> map = new HashMap<>();
            if (user != null) {
                map.put("id", user.getId());
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatar());
                map.put("username", user.getUsername());
            }
            return map;
        }).filter(m -> !m.isEmpty()).collect(Collectors.toList());

        resultPage.setRecords(records);
        resultPage.setTotal(total);
        return resultPage;
    }
}
