package com.kardexis.shared.error;

public final class InvalidCredentialsException extends KardexisException {

    private static final String DEFAULT_MESSAGE = "Invalid username/email or password";

    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidCredentialsException(String message) {
        super(message != null && !message.isBlank() ? message : DEFAULT_MESSAGE);
    }
}