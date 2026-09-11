package com.kardexis.shared.error;

public sealed abstract class KardexisException extends RuntimeException
        permits ValidationFailedException, ResourceNotFoundException,
        ForbiddenOperationException, ResourceConflictException {
    protected KardexisException(String message) {
        super(message);
    }
}