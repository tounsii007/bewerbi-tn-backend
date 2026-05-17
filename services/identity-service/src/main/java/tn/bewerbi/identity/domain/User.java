package tn.bewerbi.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserRole role = UserRole.APPLICANT;
    @Column(name = "email_verified", nullable = false) private boolean emailVerified;
    @Column(name = "email_verification_token") private String emailVerificationToken;
    @Column(name = "email_verification_expires_at") private Instant emailVerificationExpiresAt;
    @Column(name = "preferred_locale", nullable = false) private String preferredLocale = "de";
    @Column(name = "last_login_at") private Instant lastLoginAt;

    protected User() {}

    public User(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash = h; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isEmailVerified() { return emailVerified; }
    public String getEmailVerificationToken() { return emailVerificationToken; }
    public Instant getEmailVerificationExpiresAt() { return emailVerificationExpiresAt; }
    public void setEmailVerification(String token, Instant expiresAt) {
        this.emailVerificationToken = token; this.emailVerificationExpiresAt = expiresAt;
    }
    public void markEmailVerified() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiresAt = null;
    }
    public String getPreferredLocale() { return preferredLocale; }
    public void setPreferredLocale(String v) { this.preferredLocale = v; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void touchLogin() { this.lastLoginAt = Instant.now(); }
}
