package com.dong.urlshortener.mapper;

import com.dong.urlshortener.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 找到的用户对象，如果不存在则返回 null
     */
    User findByUsername(String username);

    /**
     * 插入一个新用户
     * @param user 包含用户名和密码哈希的用户对象
     */
    void insertUser(User user);

}
