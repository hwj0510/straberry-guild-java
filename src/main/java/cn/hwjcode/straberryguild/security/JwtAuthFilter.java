package cn.hwjcode.straberryguild.security;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.Claim;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * JWT 认证过滤器
 * 每个请求执行一次，从 Header 中提取并验证 Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头获取 Token
        String header = request.getHeader(jwtProperties.getHeader());
        String token = jwtUtil.extractToken(header);

        if (StrUtil.isNotBlank(token) && jwtUtil.validateToken(token)) {
            try {
                // 2. 解析 Token 中的用户信息
                Map<String, Claim> claims = jwtUtil.parseToken(token);
                Long userId = claims.get("userId").asLong();
                String username = claims.get("username").asString();
                String role = claims.get("role") != null ? claims.get("role").asString() : "USER";

                // 3. 构建认证对象，放入 SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId, // principal: 用 userId 方便后续获取
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("用户认证成功: userId={}, username={}", userId, username);
            } catch (Exception e) {
                log.warn("Token 解析失败: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
