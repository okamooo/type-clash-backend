package com.example.typing.exception;

public class InvalidImageFileException extends RuntimeException {
    public InvalidImageFileException(String message) {
        super(message);
    }
}
