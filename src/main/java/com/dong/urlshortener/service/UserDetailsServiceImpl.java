package com.dong.urlshortener.service;

import com.dong.urlshortener.mapper.UserMapper;
import com.dong.urlshortener.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 使用我们之前创建的 UserMapper 从数据库中查找用户
        User user = userMapper.findByUsername(username);

        // 2. 如果用户不存在，必须抛出此异常
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // 3. 如果用户存在，将我们自己的 User 对象转换成 Spring Security 需要的 UserDetails 对象
        //    - user.getUsername(): 用户名
        //    - user.getPasswordHash(): 从数据库取出的、已经加密过的密码
        //    - new ArrayList<>(): 用户的权限列表（我们暂时不涉及复杂的角色权限，所以给一个空的列表）
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                new ArrayList<>()
        );
    }
}