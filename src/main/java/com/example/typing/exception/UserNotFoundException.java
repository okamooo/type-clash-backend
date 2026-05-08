package com.example.typing.exception;

// ユーザーが見つからない場合の例外（論理削除済みを含む）
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("ユーザーが見つかりません。ID: " + userId);
    }
}
