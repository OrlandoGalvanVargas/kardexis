package com.kardexis.auth.domain;

import com.kardexis.shared.error.ResourceConflictException;

public class UserAlreadyExistsException extends ResourceConflictException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException("User with email '" + email + "' already exists");
    }

    public static UserAlreadyExistsException withUsername(String username) {
        return new UserAlreadyExistsException("User with username '" + username + "' already exists");
    }
}