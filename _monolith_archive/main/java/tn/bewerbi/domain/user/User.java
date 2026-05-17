package tn.bewerbi.domain.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.APPLICANT;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 64)
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private Instant emailVerificationExpiresAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_refresh_tokens", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "token_hash")
    private Set<String> refreshTokenHashes = new HashSet<>();

    protected User() {}

    public User(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isEmailVerified() { return emailVerified; }
    public void markEmailVerified() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiresAt = null;
    }
    public String getEmailVerificationToken() { return emailVerificationToken; }
    public Instant getEmailVerificationExpiresAt() { return emailVerificationExpiresAt; }
    public void setEmailVerification(String token, Instant expiresAt) {
        this.emailVerificationToken = token;
        this.emailVerificationExpiresAt = expiresAt;
    }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void touchLogin() { this.lastLoginAt = Instant.now(); }
    public Set<String> getRefreshTokenHashes() { return refreshTokenHashes; }
}
