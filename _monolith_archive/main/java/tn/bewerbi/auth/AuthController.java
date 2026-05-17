package tn.bewerbi.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (applicant or employer)")
    public AuthService.AuthResponse register(@Valid @RequestBody AuthService.RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Sign in with email + password")
    public AuthService.AuthResponse login(@Valid @RequestBody AuthService.LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new access + refresh pair")
    public AuthService.AuthResponse refresh(@RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token")
    public void logout(@RequestBody RefreshRequest req) {
        authService.logout(CurrentUser.id(), req.refreshToken());
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email via token from the verification mail")
    public void verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
    }

    public record RefreshRequest(String refreshToken) {}
}
