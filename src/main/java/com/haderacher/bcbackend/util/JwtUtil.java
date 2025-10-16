package com.haderacher.bcbackend.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Optional;


@Component
@Slf4j
public class JwtUtil {

    private final String jwtSecret;

    private final int sessionTime;

    private final SecretKey secretKey;

    @Autowired
    public JwtUtil(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.sessionTime}") int sessionTime) {
        this.jwtSecret = jwtSecret;
        this.sessionTime = sessionTime;
        this.secretKey = new SecretKeySpec(jwtSecret.getBytes(), 0, jwtSecret.getBytes().length, "HmacSHA256");
    }

    /**
     * 将<code>Student</code>类转化为<code>JWT Token</code>，这个token包含签发者issuer，用户名subject、用户角色role和过期时间戳expiration四个信息，作为payload
     * @param user 需要被转化的用户
     * @return <code>token</code> JWT Token
     */
    public String toToken(UserDetails user) {
        return Jwts.builder()
                .issuer("BCBackend")
                .subject(user.getUsername())
                .claim("roles", user.getAuthorities())
                .signWith(secretKey)
                .expiration(expireTimeFromNow())
                .compact();
    }

    public Optional<String> getStubFromToken(String token) {
        try {
            String subject = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(subject);
        } catch (JwtException e) {
            log.info("can't do JWT Authentication, {}", e.toString());
            return Optional.empty();
        }
    }

    private Date expireTimeFromNow() {
        return new Date(System.currentTimeMillis() + sessionTime * 1000L);
    }
}