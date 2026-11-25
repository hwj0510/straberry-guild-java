package cn.hwjcode.straberryguild.user.controller;


import cn.hwjcode.straberryguild.common.results.Result;
import cn.hwjcode.straberryguild.pojo.vo.UserInfoVO;
import cn.hwjcode.straberryguild.security.JwtProperties;
import cn.hwjcode.straberryguild.security.JwtUtil;
import cn.hwjcode.straberryguild.pojo.dto.*;
import cn.hwjcode.straberryguild.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户接口控制器
 * 基础路径: /api/user
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    // ==================== 公开接口（无需登录） ====================

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(userService.register(request), "注册成功");
    }

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request), "登录成功");
    }

    /**
     * 刷新 Token
     * POST /api/user/refresh
     * Body: { "refreshToken": "xxx" }
     */
    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return Result.ok(userService.refreshToken(refreshToken), "刷新成功");
    }

    /**
     * 忘记密码 - 校验账号与注册邮箱是否匹配
     * POST /api/user/forgot-password/verify
     */
    @PostMapping("/forgot-password/verify")
    public Result<Void> verifyIdentity(@Valid @RequestBody ForgotPasswordVerifyRequest request) {
        userService.verifyIdentity(request.getUsername(), request.getEmail());
        return Result.ok(null, "验证通过，请设置新密码");
    }

    /**
     * 忘记密码 - 重置密码
     * POST /api/user/forgot-password/reset
     */
    @PostMapping("/forgot-password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPasswordByEmail(request);
        return Result.ok(null, "密码重置成功，请用新密码登录");
    }

    // ==================== 需要登录的接口 ====================

    /**
     * 获取当前登录用户信息
     * GET /api/user/me
     */
    @GetMapping("/me")
    public Result<UserInfoVO> getCurrentUser(@AuthenticationPrincipal Long userId) {
        return Result.ok(userService.getUserById(userId));
    }

    /**
     * 根据 ID 获取用户信息
     * GET /api/user/{userId}
     */
    @GetMapping("/{userId}")
    public Result<UserInfoVO> getUserById(@PathVariable Long userId) {
        return Result.ok(userService.getUserById(userId));
    }

    /**
     * 更新个人资料
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(@AuthenticationPrincipal Long userId,
                                            @Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(userId, request), "更新成功");
    }

    /**
     * 关注 / 取消关注用户
     * POST /api/user/{userId}/follow
     */
    @PostMapping("/{userId}/follow")
    public Result<Map<String, Object>> toggleFollow(@AuthenticationPrincipal Long currentUserId,
                                                    @PathVariable Long userId) {
        return Result.ok(userService.toggleFollow(currentUserId, userId));
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return Result.ok();
    }
}
