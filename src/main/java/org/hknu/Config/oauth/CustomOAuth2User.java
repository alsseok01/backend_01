package org.hknu.Config.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final String email;
    private final String role;

    public CustomOAuth2User(OAuth2User oAuth2User, String email, String role) {
        this.oAuth2User = oAuth2User;
        this.email = email;
        this.role = role;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getName() {
        // 일관성을 위해 이메일을 principal의 이름으로 사용합니다.
        return email;
    }
}