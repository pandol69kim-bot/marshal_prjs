package com.example.app.domain.auth.service;

import com.example.app.common.exception.BusinessException;
import com.example.app.common.exception.ErrorCode;
import com.example.app.domain.auth.dto.*;
import com.example.app.domain.auth.entity.User;
import com.example.app.domain.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user, response);
    }

    public TokenResponse refresh(String refreshToken, HttpServletResponse response) {
        Claims claims;
        try {
            claims = jwtProvider.validateToken(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        String userId = claims.getSubject();

        // Redis에서 블랙리스트 체크
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + refreshToken))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        String newAccessToken = jwtProvider.generateAccessToken(
                userId, user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        // 기존 refresh 토큰 블랙리스트 등록
        redisTemplate.opsForValue().set(
                "blacklist:" + refreshToken, "1",
                Duration.ofSeconds(jwtProvider.getRefreshTokenExpiry()));

        setRefreshTokenCookie(response, newRefreshToken);
        return new TokenResponse(newAccessToken, "Bearer", jwtProvider.getAccessTokenExpiry());
    }

    public void logout(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + refreshToken, "1",
                    Duration.ofSeconds(jwtProvider.getRefreshTokenExpiry()));
        }
        clearRefreshTokenCookie(response);
    }

    private LoginResponse issueTokens(User user, HttpServletResponse response) {
        String userId = user.getId().toString();
        String accessToken = jwtProvider.generateAccessToken(
                userId, user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        setRefreshTokenCookie(response, refreshToken);

        return new LoginResponse(accessToken, "Bearer",
                jwtProvider.getAccessTokenExpiry(), UserDto.from(user));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영에서는 true
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) jwtProvider.getRefreshTokenExpiry());
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
