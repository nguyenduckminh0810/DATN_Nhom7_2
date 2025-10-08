package com.auro.auro.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException() {
        super("Bạn không có quyền truy cập tài nguyên này");
    }
}
