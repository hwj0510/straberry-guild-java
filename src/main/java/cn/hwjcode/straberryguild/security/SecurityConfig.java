package cn.hwjcode.straberryguild.security;

import cn.hwjcode.straberryguild.common.results.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    /**
     * 密码编码器 - BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 关闭（使用 Token 无状态认证）
                .csrf(AbstractHttpConfigurer::disable)
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Session 无状态
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 登录注册 / 忘记密码
                        .requestMatchers("/api/user/login", "/api/user/register", "/api/user/refresh",
                                "/api/user/forgot-password/**").permitAll()
                        // 公开接口 - 帖子/标签只读
                        .requestMatchers("/api/posts", "/api/posts/**", "/api/tags", "/api/tags/**", "/api/hot").permitAll()
                        // 公开接口 - 静态资源
                        .requestMatchers("/", "/index.html", "/static/**", "/uploads/**", "/error").permitAll()
                        // WebSocket 握手（token 走 query 参数，端点内自行鉴权）
                        .requestMatchers("/ws/**").permitAll()
                        // Swagger / Actuator（可选）
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                // JWT 过滤器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 异常处理
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, resp, e) -> writeJson(resp, Result.fail(401, "未登录或 Token 已过期")))
                        .accessDeniedHandler((req, resp, e) -> writeJson(resp, Result.fail(403, "无权限访问")))
                );

        return http.build();
    }

    /**
     * CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 输出 JSON 响应
     */
    private void writeJson(jakarta.servlet.http.HttpServletResponse response, Object obj) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            response.getWriter().write(objectMapper.writeValueAsString(obj));
        } catch (Exception e) {
            log.error("写入响应失败", e);
        }
    }
}