package com.kardexis.shared.error;

public sealed abstract class KardexisException extends RuntimeException
        permits ValidationFailedException, ResourceNotFoundException,
        ForbiddenOperationException, ResourceConflictException,
        InvalidCredentialsException {

    protected KardexisException(String message) {
        super(message);
    }

    protected KardexisException(String message, Throwable cause) {
        super(message, cause);
    }

}