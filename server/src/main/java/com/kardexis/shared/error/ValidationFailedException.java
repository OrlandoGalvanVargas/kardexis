package com.kardexis.shared.error;

public final class ValidationFailedException extends KardexisException {
    public ValidationFailedException(String message) {
        super(message);
    }
}