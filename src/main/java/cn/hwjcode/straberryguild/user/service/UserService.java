package cn.hwjcode.straberryguild.user.service;


import cn.hwjcode.straberryguild.pojo.dto.*;
import cn.hwjcode.straberryguild.pojo.entity.User;
import cn.hwjcode.straberryguild.pojo.vo.UserInfoVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserInfoVO register(RegisterRequest request);

    /**
     * 用户登录，返回 Token
     */
    TokenResponse login(LoginRequest request);

    /**
     * 刷新 Token
     */
    TokenResponse refreshToken(String refreshTokenStr);

    /**
     * 根据 ID 获取用户信息
     */
    UserInfoVO getUserById(Long userId);

    /**
     * 根据 ID 获取实体（内部使用）
     */
    User getEntityById(Long userId);

    /**
     * 更新个人资料
     */
    UserInfoVO updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 忘记密码 - 校验账号与注册邮箱是否匹配
     */
    void verifyIdentity(String username, String email);

    /**
     * 忘记密码 - 校验通过后重置密码
     */
    void resetPasswordByEmail(ResetPasswordRequest request);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);

    /**
     * 关注 / 取消关注用户
     * @return following：操作后是否处于已关注状态
     */
    java.util.Map<String, Object> toggleFollow(Long userId, Long targetUserId);
}
