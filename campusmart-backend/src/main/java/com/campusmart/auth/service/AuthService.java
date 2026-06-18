package com.campusmart.auth.service;

import com.campusmart.auth.dto.AuthResponseDto;
import com.campusmart.auth.dto.LoginRequestDto;
import com.campusmart.auth.dto.RegisterRequestDto;
import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ResourceNotFoundException;
import com.campusmart.role.entity.Role;
import com.campusmart.role.entity.RoleName;
import com.campusmart.role.repository.RoleRepository;
import com.campusmart.security.JwtService;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<RoleName> DEFAULT_ROLES = Set.of(RoleName.BUYER);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered");
        }

        Set<Role> roles = resolveRoles(request.roles());
        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .profileImage(request.profileImage())
                .isActive(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        return buildAuthResponse(user);
    }

    private Set<Role> resolveRoles(Set<RoleName> requestedRoles) {
        Set<RoleName> roleNames = requestedRoles == null || requestedRoles.isEmpty()
                ? DEFAULT_ROLES
                : requestedRoles;

        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
    }

    private AuthResponseDto buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        Set<RoleName> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new AuthResponseDto(
                token,
                "Bearer",
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                roles
        );
    }
}

