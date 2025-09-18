package com.dong.urlshortener.service;

import com.dong.urlshortener.mapper.LinkMapper; // 更改引入
import com.dong.urlshortener.model.Link;
import com.dong.urlshortener.util.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 推荐加上事务

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
        private static final String ALPHABET = "5gZAmxPt4QOdEYs2h3jKn6vbyLcF0iWpU9aIu7Dr1BVfeCw8NqGzRkJSTXHMo";

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
    private static final long OFFSET = 1000000000L;
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
            String shortCode = Base62.fromBase10(newLink.getId()+OFFSET);
            newLink.setShortCode(shortCode);
            linkMapper.updateShortCode(newLink);

            // 将新的映射关系存入 Redis，并设置一个匹配的过期时间！
            //redisTemplate.opsForValue().set(shortCode, longUrl, DEFAULT_EXPIRATION_HOURS, TimeUnit.HOURS);

            return "http://localhost:8080/" + shortCode;
        }
    }

    /**
     * 根据 shortCode 获取长链接。
     * 这是一个高性能的查询，会优先访问 Redis 缓存。
     * @param shortCode 短码
     * @return 原始的长链接，如果找不到则返回 null
     */
    public String getLongUrlByShortCode(String shortCode) {
        // 1. 先从 Redis 缓存中查找
//        String longUrl = redisTemplate.opsForValue().get(shortCode);
//
//        if (longUrl != null) {
//            System.out.println("缓存命中: " + shortCode + " -> " + longUrl);
//            return longUrl;
//        }

        // 2. 缓存未命中，则从数据库查找
        System.out.println("缓存未命中，查询数据库: " + shortCode);
        Link link = linkMapper.findByShortCode(shortCode); // 使用我们已有的 findByShortCode 方法

        if (link != null) {
            // 3. 在数据库中找到了，回填到缓存中，方便下次访问
            // 注意：这里可以设置一个与数据库记录相匹配的过期时间
//            long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), link.getExpiresAt());
//            if (remainingSeconds > 0) {
//                redisTemplate.opsForValue().set(link.getShortCode(), link.getLongUrl(), remainingSeconds, TimeUnit.SECONDS);
//            }
            return link.getLongUrl();
        }

        // 4. 数据库里也找不到
        return null;
    }
}