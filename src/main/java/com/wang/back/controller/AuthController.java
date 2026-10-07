package com.wang.back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.back.common.Result;
import com.wang.back.entity.User;
import com.wang.back.mapper.UserMapper;
import com.wang.back.common.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    //密码加密器：注册时加密密码、登录时校验密码
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //注册
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        //校验注册信息
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return Result.error("密码至少 6 位");
        }

        //检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        //加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }

        //写入数据库
        userMapper.insert(user);
        return Result.success(null);
    }

    //登录
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        //根据用户名查询
        User dbUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (dbUser == null) {
            return Result.error("用户名或密码错误");
        }

        //校验密码
        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        //生成Token
        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername());

        //返回Token+用户信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", Map.of(
                "id", dbUser.getId(),
                "username", dbUser.getUsername(),
                "nickname", dbUser.getNickname()
        ));
        return Result.success(data);
    }
}