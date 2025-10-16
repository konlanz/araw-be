package com.araw.shared.exception;

public class DomainNotFoundException extends RuntimeException {

    public DomainNotFoundException(String message) {
        super(message);
    }
}
