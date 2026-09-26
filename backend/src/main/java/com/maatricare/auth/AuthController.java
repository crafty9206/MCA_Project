package com.maatricare.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;

import com.maatricare.security.JwtService;
import com.maatricare.security.TokenBlacklistService;
import com.maatricare.tracking.CareTask;
import com.maatricare.tracking.CareTaskRepository;
import com.maatricare.tracking.TaskDetailRepository;
import com.maatricare.user.User;
import com.maatricare.user.AccountService;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AccountService accountService;
    private final PasswordResetService passwordResetService;
    private final CareTaskRepository careTaskRepository;
    private final TaskDetailRepository taskDetailRepository;
    private final LoginAttemptService loginAttemptService;
    private final boolean exposeResetToken;

    private static final String DUMMY_PASSWORD_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5t10VhZ.HA8v3zF.7j8WHJ7Qh0I8R4C";

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                TokenBlacklistService tokenBlacklistService, AccountService accountService,
                PasswordResetService passwordResetService, CareTaskRepository careTaskRepository,
                TaskDetailRepository taskDetailRepository, LoginAttemptService loginAttemptService,
                @Value("${security.password-reset.expose-token:false}") boolean exposeResetToken) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.accountService = accountService;
        this.passwordResetService = passwordResetService;
        this.careTaskRepository = careTaskRepository;
        this.taskDetailRepository = taskDetailRepository;
        this.loginAttemptService = loginAttemptService;
        this.exposeResetToken = exposeResetToken;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()), request.displayName().trim()));
        ensureDefaultTasks(user, LocalDate.now());
        return responseFor(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.email().trim().toLowerCase();
        String attemptKey = email + "|" + httpRequest.getRemoteAddr();
        if (loginAttemptService.isBlocked(attemptKey)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed login attempts. Try again in "
                            + loginAttemptService.retryAfterSeconds(attemptKey) + " seconds.");
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        boolean passwordMatches = passwordEncoder.matches(request.password(),
                user == null ? DUMMY_PASSWORD_HASH : user.getPasswordHash());
        if (user == null || !passwordMatches) {
            loginAttemptService.recordFailure(attemptKey);
            throw new BadCredentialsException("Invalid email or password");
        }
        loginAttemptService.recordSuccess(attemptKey);
        return responseFor(user);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = passwordResetService.createToken(request.email());
        return new ForgotPasswordResponse("If the account exists, reset instructions have been created.",
                exposeResetToken ? token : null);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.password());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return;
        String token = authorization.substring(7);
        tokenBlacklistService.revoke(token, jwtService.extractExpiration(token));
    }

    @DeleteMapping("/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Authentication authentication,
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        accountService.deleteByEmail(authentication.getName());
        tokenBlacklistService.revoke(token, jwtService.extractExpiration(token));
    }

    private AuthResponse responseFor(User user) {
        return new AuthResponse(jwtService.issueToken(user.getEmail(), user.getRole()), user.getEmail(), user.getDisplayName(), user.getRole());
    }

    private void ensureDefaultTasks(User user, LocalDate taskDate) {
        java.util.List.of(
                "Take prenatal vitamins",
                "Drink 8 glasses of water",
                "Take a 20 minute walk",
                "Get enough rest"
        ).forEach(title -> {
            var taskDetail = taskDetailRepository.findByTitleIgnoreCase(title)
                    .orElseGet(() -> taskDetailRepository.save(new com.maatricare.tracking.TaskDetail(title, true)));
            if (!careTaskRepository.existsByUserIdAndTaskDateAndTaskDetailId(user.getId(), taskDate, taskDetail.getId())) {
                careTaskRepository.save(new com.maatricare.tracking.CareTask(user, taskDetail, taskDate));
            }
        });
    }

    public record RegisterRequest(@Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 120) String displayName) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

        public record ForgotPasswordRequest(@Email @NotBlank String email) {
        }

        public record ForgotPasswordResponse(String message, String resetToken) {
        }

        public record ResetPasswordRequest(@NotBlank String token,
            @NotBlank @Size(min = 8, max = 72) String password) {
        }

    public record AuthResponse(String token, String email, String displayName, String role) {
    }
}