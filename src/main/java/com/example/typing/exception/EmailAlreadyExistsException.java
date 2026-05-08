package com.example.typing.exception;

// メールアドレスが既に登録されている場合の例外
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("このメールアドレスは既に登録されています。email: " + email);
    }
}
