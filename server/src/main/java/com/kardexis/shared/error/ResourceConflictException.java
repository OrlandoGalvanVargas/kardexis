package com.kardexis.shared.error;

public non-sealed class ResourceConflictException extends KardexisException {
    public ResourceConflictException(String message) {
        super(message);
    }
}