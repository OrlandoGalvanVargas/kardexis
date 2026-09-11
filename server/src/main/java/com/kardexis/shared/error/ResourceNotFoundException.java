package com.kardexis.shared.error;

public final class ResourceNotFoundException extends KardexisException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}