package com.example.app.domain.auth.handler;

import com.example.app.domain.auth.entity.User;
import com.example.app.domain.auth.repository.UserRepository;
import com.example.app.domain.auth.service.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Provider별 email 추출 — Google은 최상위 "email", Kakao는 kakao_account.email
        String email = extractEmail(registrationId, oAuth2User);

        if (email == null) {
            log.error("OAuth2 이메일을 가져올 수 없습니다. provider={}", registrationId);
            getRedirectStrategy().sendRedirect(request, response,
                    redirectUri + "?error=email_required");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OAuth2 사용자를 찾을 수 없습니다: " + email));

        String userId = user.getId().toString();
        String accessToken  = jwtProvider.generateAccessToken(userId, user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        // 이메일 로그인과 동일하게 refresh token을 HttpOnly 쿠키로 설정
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영 환경에서는 true
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) jwtProvider.getRefreshTokenExpiry());
        response.addCookie(cookie);

        String targetUrl = redirectUri + "?token=" + accessToken;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String extractEmail(String registrationId, OAuth2User oAuth2User) {
        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount == null) return null;
            return (String) kakaoAccount.get("email");
        }
        // Google, 기타 OIDC provider
        return oAuth2User.getAttribute("email");
    }
}
