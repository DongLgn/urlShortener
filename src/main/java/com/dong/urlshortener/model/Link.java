package com.dong.urlshortener.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Link {
    private Long id;
    private String longUrl;
    private String longUrlHash;
    private String shortCode;
    private Integer clickCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;
    private Long userId;
}