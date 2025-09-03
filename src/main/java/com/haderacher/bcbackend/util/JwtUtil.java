package com.haderacher.bcbackend.util;

import com.haderacher.bcbackend.entity.aggregates.recruiter.Recruiter;
import com.haderacher.bcbackend.entity.aggregates.student.Student;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public String toToken(Student student) {
        return Jwts.builder()
                .issuer("BCBackend")
                .subject(student.getUsername())
                .signWith(secretKey)
                .expiration(expireTimeFromNow())
                .compact();
    }

    public String toToken(Recruiter recruiter) {
        return Jwts.builder()
                .issuer("BCBackend")
                .subject(recruiter.getUsername())
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