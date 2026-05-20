package com.example.typing.exception;

/**
 * OTP の発行・検証処理で問題が発生した場合にスローする例外。
 * 有効期限切れ、コード不一致、試行回数超過などが該当する。
 */
public class OtpAuthenticationException extends RuntimeException {

    /**
     * @param message ユーザーへ返却するエラーメッセージ
     */
    public OtpAuthenticationException(String message) {
        super(message);
    }
}
