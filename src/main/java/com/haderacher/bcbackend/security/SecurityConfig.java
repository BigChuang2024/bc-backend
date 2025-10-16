package com.haderacher.bcbackend.security;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenFilter jwtTokenFilter) throws Exception {
        http
                .sessionManagement(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/students/register", "/students/login", "/actuator/*", "/ai/*").permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 关键：使用 allowedOriginPatterns 来支持通配符
        // 允许来自 10.150.x.x 网段的所有源 (http 和 https)
        configuration.setAllowedOriginPatterns(
                List.of("http://10.150.*.*:[*]",
                        "https://10.150.*.*:[*]",
                        "http://localhost:[*]",
                        "http://127.0.0.1:[*]"
                ));

        // 允许所有请求方法 (GET, POST, PUT, DELETE 等)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许所有请求头
        configuration.setAllowedHeaders(List.of("*"));

        // 允许浏览器发送凭证 (如 cookies)
        configuration.setAllowCredentials(true);
        configuration.setAllowPrivateNetwork(true);

        // 设置预检请求 (OPTIONS) 的缓存时间，单位为秒
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 API 路径应用这个CORS配置
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    @Bean
    public FilterRegistrationBean<JwtTokenFilter> tenantFilterRegistration(JwtTokenFilter filter) {
        FilterRegistrationBean<JwtTokenFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
