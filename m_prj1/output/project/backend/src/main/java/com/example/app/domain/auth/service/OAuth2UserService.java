package com.example.app.domain.auth.service;

import com.example.app.domain.auth.entity.User;
import com.example.app.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email;
        String name;
        String providerId;
        User.Provider provider;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("sub");
            provider = User.Provider.GOOGLE;
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            providerId = String.valueOf(oAuth2User.getAttribute("id"));
            provider = User.Provider.KAKAO;
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 Provider: " + registrationId);
        }

        final String finalEmail = email;
        final String finalName = name;
        final String finalProviderId = providerId;
        final User.Provider finalProvider = provider;

        userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.createOAuth2(finalEmail, finalName, finalProvider, finalProviderId)));

        return oAuth2User;
    }
}
