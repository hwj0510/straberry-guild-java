package cn.hwjcode.straberryguild.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /** Access Token */
    private String accessToken;

    /** Refresh Token */
    private String refreshToken;

    /** Token 类型 */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Access Token 过期时间（秒） */
    private Long expiresIn;
}
