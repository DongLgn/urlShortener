package com.dong.urlshortener.controller;

import com.dong.urlshortener.dto.ShortenRequest;
import com.dong.urlshortener.service.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class LinkController {

    @Autowired
    private LinkService linkService;

    @PostMapping("/api/shorten")
    public ResponseEntity<String> createShortLink(@RequestBody ShortenRequest request) {
        String shortLink = linkService.getOrCreateShortLink(request.getLongUrl());
        return ResponseEntity.ok(shortLink);
    }
    /**
     * 根据 shortCode 重定向到原始的长链接。
     * @param shortCode 从 URL 路径中获取的短码
     * @param response  Spring 注入的 HTTP 响应对象，用于执行重定向
     */
    @GetMapping("/{shortCode}")
    public void redirectToLongUrl(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        // 1. 调用 Service 层，根据短码查找长链接
        String longUrl = linkService.getLongUrlByShortCode(shortCode);

        // 2. 检查是否找到了长链接
        if (longUrl != null) {
            // 3. 如果找到了，使用 HttpServletResponse 执行 302 临时重定向
            System.out.println("找到链接，重定向到: " + longUrl);
            response.sendRedirect(longUrl);
        } else {
            // 4. 如果没找到，抛出一个 404 Not Found 错误
            System.out.println("未找到短链接: " + shortCode);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found for code: " + shortCode);
        }
    }
}