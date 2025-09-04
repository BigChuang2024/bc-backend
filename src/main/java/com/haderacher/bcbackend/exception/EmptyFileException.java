package com.haderacher.bcbackend.exception;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException(String message) {
        super(message);
    }
}
