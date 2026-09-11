package com.kardexis.shared.error;

public final class ForbiddenOperationException extends KardexisException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}