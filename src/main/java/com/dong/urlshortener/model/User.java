package com.dong.urlshortener.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {

    private Long id;
    private String username;
    private String passwordHash; // 对应数据库的 password_hash
    private String email;
    private LocalDateTime createdAt; // 对应数据库的 created_at

}
