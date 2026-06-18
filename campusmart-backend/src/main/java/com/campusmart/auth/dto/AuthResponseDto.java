package com.campusmart.auth.dto;

import com.campusmart.role.entity.RoleName;
import java.util.Set;

public record AuthResponseDto(
        String token,
        String tokenType,
        Long userId,
        String firstName,
        String lastName,
        String email,
        Set<RoleName> roles
) {
}

