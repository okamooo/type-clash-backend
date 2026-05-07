package com.example.typing.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("ユーザーが見つかりません。ID: " + userId);
    }
}
