package cn.hwjcode.straberryguild.user.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hwjcode.straberryguild.common.exception.BusinessException;
import cn.hwjcode.straberryguild.pojo.vo.UserInfoVO;
import cn.hwjcode.straberryguild.security.JwtProperties;
import cn.hwjcode.straberryguild.security.JwtUtil;
import cn.hwjcode.straberryguild.pojo.dto.*;
import cn.hwjcode.straberryguild.pojo.entity.User;
import cn.hwjcode.straberryguild.pojo.entity.UserFollow;
import cn.hwjcode.straberryguild.post.mapper.UserFollowMapper;
import cn.hwjcode.straberryguild.user.mapper.UserMapper;
import cn.hwjcode.straberryguild.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Override
    @Transactional
    public UserInfoVO register(RegisterRequest request) {
        // 1. 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw BusinessException.badRequest("两次密码不一致");
        }

        // 2. 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw BusinessException.badRequest("用户名已被注册");
        }

        // 3. 检查邮箱是否已注册（如果填写）
        if (StrUtil.isNotBlank(request.getEmail())) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())
            );
            if (emailCount > 0) {
                throw BusinessException.badRequest("邮箱已被注册");
            }
        }

        // 4. 构建用户实体
        User user = new User();
        BeanUtils.copyProperties(request, user, "confirmPassword", "password", "nickname");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StrUtil.isBlank(request.getNickname()) ? request.getUsername() : request.getNickname());
        user.setLevel(1);
        user.setExp(0);
        user.setRole("USER");
        user.setStatus(1);
        user.setDeleted(0);

        userMapper.insert(user);
        log.info("用户注册成功: username={}, userId={}", user.getUsername(), user.getId());

        return UserInfoVO.fromEntity(user);
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 1. 查找用户（账号可能是用户名/邮箱）
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getAccount())
                .or()
                .eq(User::getEmail, request.getAccount())
                .last("LIMIT 1")
        );

        if (user == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 3. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 4. 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .build();
    }

    @Override
    public TokenResponse refreshToken(String refreshTokenStr) {
        // 1. 验证 Token
        if (!jwtUtil.validateToken(refreshTokenStr)) {
            throw BusinessException.unauthorized("Refresh Token 无效或已过期");
        }

        // 2. 必须是 Refresh Token 类型
        if (!jwtUtil.isRefreshToken(refreshTokenStr)) {
            throw BusinessException.badRequest("请使用 Refresh Token 刷新");
        }

        // 3. 获取用户
        Long userId = jwtUtil.getUserId(refreshTokenStr);
        User user = getEntityById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 4. 生成新 Token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .build();
    }

    @Override
    public UserInfoVO getUserById(Long userId) {
        User user = getEntityById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return UserInfoVO.fromEntity(user);
    }

    @Override
    public User getEntityById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional
    public UserInfoVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getEntityById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        String nickname = request.getNickname().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        if (StrUtil.isBlank(email)) {
            email = null;
        }
        if (email != null) {
            Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email)
                    .ne(User::getId, userId));
            if (emailCount > 0) {
                throw BusinessException.badRequest("邮箱已被其他用户使用");
            }
        }

        user.setNickname(nickname);
        user.setEmail(email);
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getNickname, nickname)
                .set(User::getEmail, email);

        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
            updateWrapper.set(User::getBio, user.getBio());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
            updateWrapper.set(User::getAvatar, user.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
            updateWrapper.set(User::getGender, user.getGender());
        }

        userMapper.update(null, updateWrapper);
        return UserInfoVO.fromEntity(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getEntityById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 校验旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.badRequest("旧密码不正确");
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户修改密码成功: userId={}", userId);
    }

    @Override
    public void verifyIdentity(String username, String email) {
        // 仅校验，通过即返回；错误信息统一不区分"账号不存在/邮箱不匹配"，避免枚举用户
        findUserByUsernameAndEmail(username, email);
    }

    @Override
    @Transactional
    public void resetPasswordByEmail(ResetPasswordRequest request) {
        User user = findUserByUsernameAndEmail(request.getUsername(), request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户通过邮箱重置密码成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * 忘记密码：按账号查用户并校验注册邮箱是否匹配。
     * verify / reset 两个接口各自独立调用，保证无状态下重置操作本身也是安全的。
     */
    private User findUserByUsernameAndEmail(String username, String email) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(email)) {
            throw BusinessException.badRequest("账号和邮箱不能为空");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username.trim())
                .last("LIMIT 1"));
        if (user == null) {
            throw BusinessException.badRequest("账号或邮箱不正确");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (StrUtil.isBlank(user.getEmail())) {
            throw BusinessException.badRequest("该账号未绑定邮箱，无法通过邮箱找回");
        }
        if (!user.getEmail().trim().equalsIgnoreCase(email.trim())) {
            throw BusinessException.badRequest("账号或邮箱不正确");
        }
        return user;
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleFollow(Long userId, Long targetUserId) {
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        if (targetUserId == null || userMapper.selectById(targetUserId) == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (targetUserId.equals(userId)) {
            throw BusinessException.badRequest("不能关注自己");
        }

        UserFollow existing = userFollowMapper.selectOne(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetUserId));

        boolean following;
        if (existing != null) {
            userFollowMapper.deleteById(existing.getId());
            following = false;
        } else {
            UserFollow follow = new UserFollow();
            follow.setFollowerId(userId);
            follow.setFollowingId(targetUserId);
            userFollowMapper.insert(follow);
            following = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("following", following);
        return result;
    }
}