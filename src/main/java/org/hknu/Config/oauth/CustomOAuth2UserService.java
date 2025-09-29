package org.hknu.Config.oauth;

import org.hknu.Repo.MemberRepo;
import org.hknu.entity.Member;
import org.hknu.entity.Member.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberRepo memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = null;
        String name = null;
        String picture = null;

        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            picture = (String) profile.get("profile_image_url");
        }

        Member member = processOAuth2User(registrationId, email, name, picture);

        return new CustomOAuth2User(oAuth2User, member.getEmail(), "ROLE_USER");
    }

    private Member processOAuth2User(String registrationId, String email, String name, String picture) {
        Optional<Member> userOptional = memberRepository.findByEmail(email);
        Member member;
        if (userOptional.isPresent()) {
            member = userOptional.get();
            member.setName(name);
            member.setProfileImage(picture);
        } else {
            member = new Member();
            member.setEmail(email);
            member.setName(name);
            member.setProfileImage(picture);
            member.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
        }
        return memberRepository.save(member);
    }
}