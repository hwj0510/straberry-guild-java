package cn.hwjcode.straberryguild.security;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.verifier = JWT.require(algorithm)
                .withIssuer("straberry-guild")
                .build();
    }

    public String generateAccessToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, "access", jwtProperties.getExpiration());
    }

    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, null, "refresh", jwtProperties.getRefreshExpiration());
    }

    private String buildToken(Long userId, String username, String role, String type, Long expireMillis) {
        long now = System.currentTimeMillis();
        var builder = JWT.create()
                .withIssuer("straberry-guild")
                .withClaim(CLAIM_USER_ID, userId)
                .withClaim(CLAIM_USERNAME, username)
                .withClaim(CLAIM_TYPE, type)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + expireMillis));
        if (StrUtil.isNotBlank(role)) {
            builder.withClaim(CLAIM_ROLE, role);
        }
        return builder.sign(algorithm);
    }

    public boolean validateToken(String token) {
        if (StrUtil.isBlank(token)) return false;
        try {
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            log.debug("Token verify failed: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Claim> parseToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaims();
    }

    public Long getUserId(String token) {
        return parseToken(token).get(CLAIM_USER_ID).asLong();
    }

    public String getUsername(String token) {
        return parseToken(token).get(CLAIM_USERNAME).asString();
    }

    public String getRole(String token) {
        return parseToken(token).get(CLAIM_ROLE).asString();
    }

    public boolean isRefreshToken(String token) {
        String type = parseToken(token).get(CLAIM_TYPE).asString();
        return "refresh".equals(type);
    }

    public String extractToken(String header) {
        if (StrUtil.isBlank(header)) return null;
        String prefix = jwtProperties.getTokenPrefix();
        if (header.startsWith(prefix)) {
            return header.substring(prefix.length());
        }
        return header;
    }
}