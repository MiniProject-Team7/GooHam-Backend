package com.uplus.ureka.controller;

import com.uplus.ureka.service.s3.S3Service; // ← S3Service 위치에 맞게 수정

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/gooham/images")
@AllArgsConstructor

public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/presign")
    public PresignResponse presign(@RequestParam String key) {
        // 만료 시간은 1시간(필요시 조정)
        String url = s3Service.generatePresignedUrl(key, Duration.ofHours(1));
        return new PresignResponse(url);
    }

    @Data
    static class PresignResponse {
        private final String url;
    }
}