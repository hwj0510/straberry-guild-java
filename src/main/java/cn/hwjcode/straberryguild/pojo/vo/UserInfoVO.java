package cn.hwjcode.straberryguild.pojo.vo;


import cn.hwjcode.straberryguild.pojo.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应 VO
 * (不包含敏感字段如密码)
 */
@Data
@Builder
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private String bio;
    private Integer level;
    private Integer exp;
    private String role;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;

    /** 从实体转换为 VO */
    public static UserInfoVO fromEntity(User user) {
        if (user == null) return null;
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .bio(user.getBio())
                .level(user.getLevel())
                .exp(user.getExp())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }
}
