package com.haderacher.bcbackend.security;

import com.haderacher.bcbackend.repository.RecruiterRepository;
import com.haderacher.bcbackend.repository.StudentRepository;
import com.haderacher.bcbackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private StudentRepository studentRepository;

    private RecruiterRepository recruiterRepository;

    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = "Authorization";
        getTokenString(request.getHeader(header))
                .flatMap(token -> jwtUtil.getStubFromToken(token))
                .ifPresent(token -> {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                studentRepository.findByUsername(token).ifPresent(student -> {
                    student.setPassword(null);
                    log.debug("logging student:{}", student);
                    UsernamePasswordAuthenticationToken authenticationToken
                            = new UsernamePasswordAuthenticationToken(student, null,
                            student.getAuthorities().stream().map(
                                    authority ->
                                            new SimpleGrantedAuthority(authority.toString())).collect(Collectors.toList())
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                });
            }
        });
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return "/students/login".equals(path);
    }

    private Optional<String> getTokenString(String header) {
        if (header == null) {
            return Optional.empty();
        } else {
            String[] split = header.split(" ");
            if (split.length < 2) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(split[1]);
            }
        }
    }
}
