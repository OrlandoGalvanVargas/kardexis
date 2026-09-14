package com.kardexis.auth.domain;

import com.kardexis.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
@SoftDelete(strategy = SoftDeleteType.DELETED, columnName = "deleted")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    protected User() {
    }

    public User(String username, String email, String passwordHash, String displayName) {
        this.username = requireValidUsername(username);
        this.email = requireValidEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.displayName = displayName != null ? displayName.trim() : null;
    }


    public void changePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "Password hash cannot be null");
    }

    public void updateProfile(String displayName) {
        this.displayName = displayName != null ? displayName.trim() : null;
    }


    private static String requireValidUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        String trimmed = username.trim();
        if (trimmed.length() < 3 || trimmed.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        return trimmed;
    }

    private static String requireValidEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        String trimmed = email.trim().toLowerCase();
        if (!trimmed.contains("@") || trimmed.length() > 255) {
            throw new IllegalArgumentException("Invalid email address format");
        }
        return trimmed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}