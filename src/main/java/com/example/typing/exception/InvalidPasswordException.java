package com.example.typing.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("現在のパスワードが正しくありません");
    }
}
