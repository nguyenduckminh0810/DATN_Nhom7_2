package com.auro.auro.exception;

public class UnauthorizedException extends RuntimeException {

        public UnauthorizedException(String message){
            super(message);
        }

        public UnauthorizedException(){
            super("Bạn chưa đăng nhập. Vui lòng đăng nhập để tiếp tục");
        }
}
