package cn.hwjcode.straberryguild.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 * 对应 application.yaml 中的 jwt 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 密钥 */
    private String secret;

    /** Access Token 有效期（毫秒） */
    private Long expiration = 86400000L;

    /** Refresh Token 有效期（毫秒） */
    private Long refreshExpiration = 604800000L;

    /** HTTP Header 名称 */
    private String header = "Authorization";

    /** Token 前缀 */
    private String tokenPrefix = "Bearer ";
}
