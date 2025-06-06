package com.uplus.ureka.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomExceptions extends RuntimeException {
    private final HttpStatus status;

    public CustomExceptions(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST; // 기본 상태 코드
    }

    public CustomExceptions(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}