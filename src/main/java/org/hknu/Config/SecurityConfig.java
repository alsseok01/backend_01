package org.hknu.Config;

import org.hknu.Config.oauth.CustomOAuth2UserService;
import org.hknu.Config.oauth.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.Arrays;

import static org.springframework.http.HttpMethod.OPTIONS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/api/auth/verify-email**","/ws/**", "/api/ai/**").permitAll()

                        .requestMatchers("/api/reviews/**").authenticated()

                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/api/auth/verify-email**","/ws/**", "/api/ai/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/user/profile").authenticated()
                        .requestMatchers("/api/schedules/**").authenticated()
                        .requestMatchers("/api/matches/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/board").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/board").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/board/{postId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/board/{postId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/board/{postId}/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/board/{postId}/view").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/board/{postId}/like").authenticated()
                        .requestMatchers("/api/recommendations/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/images/upload").authenticated()
                        .requestMatchers("/api/fcm/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService)
                                )
                                .successHandler(oAuth2SuccessHandler)
                );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://192.168.123.101:3000" ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}