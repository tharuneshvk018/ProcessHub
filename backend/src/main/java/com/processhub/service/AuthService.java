package com.processhub.service;

import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.AuthResponse;
import com.processhub.dto.LoginRequest;
import com.processhub.dto.RegisterRequest;
import com.processhub.dto.UserProfileResponse;
import com.processhub.exception.AuthException;
import com.processhub.exception.DuplicateResourceException;
import com.processhub.exception.ResourceNotFoundException;
import com.processhub.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + normalizedEmail + " already exists");
        }

        // SAFEGUARD 2: Public registration strictly assigns ROLE_REPORTER.
        // Administrative and privileged roles can never be self-assigned through public registration.
        Role assignedRole = Role.ROLE_REPORTER;

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                UUID.randomUUID(),
                normalizedEmail,
                encodedPassword,
                request.getFullName().trim(),
                request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null,
                assignedRole,
                null // Department is assigned later by an administrator
        );

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getDepartment()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AuthException("Account is disabled. Please contact your organization administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getDepartment()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getDepartment(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
