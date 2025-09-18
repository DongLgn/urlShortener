package com.dong.urlshortener.dto;

import lombok.Data;

@Data
public class ShortenRequest {
    private String longUrl;
}