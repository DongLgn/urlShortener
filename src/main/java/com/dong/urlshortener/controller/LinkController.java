package com.dong.urlshortener.controller;

import com.dong.urlshortener.dto.ShortenRequest;
import com.dong.urlshortener.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}