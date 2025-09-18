package com.dong.urlshortener.service;

import com.dong.urlshortener.mapper.LinkMapper; // 更改引入
import com.dong.urlshortener.model.Link;
import com.dong.urlshortener.util.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 推荐加上事务

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class LinkService {

    // 注入 LinkMapper
    @Autowired
    private LinkMapper linkMapper;
    private static final long DEFAULT_EXPIRATION_HOURS = 24;
    // 使用 Base62 工具类 (代码与之前版本相同)
    // ...
    public class Base62 {
        private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        private static final int BASE = ALPHABET.length();

        public static String fromBase10(long i) {
            if (i == 0) return "a"; // 防止 id 为 0 时返回空字符串
            StringBuilder sb = new StringBuilder();
            while (i > 0) {
                i = fromBase10(i, sb);
            }
            return sb.reverse().toString();
        }

        private static long fromBase10(long i, final StringBuilder sb) {
            int rem = (int) (i % BASE);
            sb.append(ALPHABET.charAt(rem));
            return i / BASE;
        }
    }
    @Transactional
    public String getOrCreateShortLink(String longUrl) {
        // 1. 计算长链接的哈希值
        String longUrlHash = HashUtils.sha256(longUrl);

        // 2. 使用新的查询方法，查找一个【未过期】的链接
        Link existingActiveLink = linkMapper.findActiveByLongUrlHash(longUrlHash);

        // 3. 【核心逻辑判断】
        if (existingActiveLink != null) {
            // 3a. 如果找到了一个未过期的链接，直接返回它的短链接
            System.out.println("找到了有效的短链接，直接返回: " + existingActiveLink.getShortCode());
            return "http://localhost:8080/" + existingActiveLink.getShortCode();
        } else {
            // 3b. 如果没找到（说明要么是新链接，要么是已过期的链接），则创建新的
            System.out.println("未找到有效短链接，开始创建新的...");
            Link newLink = new Link();
            newLink.setLongUrl(longUrl);
            newLink.setLongUrlHash(longUrlHash);

            // 计算过期时间
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(DEFAULT_EXPIRATION_HOURS);
            newLink.setExpiresAt(expiresAt);

            // 插入数据库 (现在会包含 hash 和过期时间)
            linkMapper.insertLink(newLink);

            // 生成短码并更新回数据库
            String shortCode = Base62.fromBase10(newLink.getId());
            newLink.setShortCode(shortCode);
            linkMapper.updateShortCode(newLink);

            // 将新的映射关系存入 Redis，并设置一个匹配的过期时间！
            //redisTemplate.opsForValue().set(shortCode, longUrl, DEFAULT_EXPIRATION_HOURS, TimeUnit.HOURS);

            return "http://localhost:8080/" + shortCode;
        }
    }

    public String getLongUrlByShortCode(String shortCode) {
        // TODO: 先从 Redis 缓存中查找

        // 缓存未命中，则从数据库查找
        Link link = linkMapper.findByShortCode(shortCode);
        return (link != null) ? link.getLongUrl() : null;
    }
}