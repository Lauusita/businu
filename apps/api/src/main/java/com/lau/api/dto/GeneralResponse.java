package com.lau.api.dto;

import org.springframework.http.HttpStatus;

public record GeneralResponse(
        HttpStatus status,
        String message
) { }