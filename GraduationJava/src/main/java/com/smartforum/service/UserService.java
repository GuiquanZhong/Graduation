package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartforum.entity.User;
import com.smartforum.mapper.UserMapper;
import com.smartforum.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    public void register(String username, String password, String nickname) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        userMapper.insert(user);
    }

    /**
     * 用户登录，返回 Token 和用户信息
     */
    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        return result;
    }

    /**
     * 根据ID获取用户信息
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
}
