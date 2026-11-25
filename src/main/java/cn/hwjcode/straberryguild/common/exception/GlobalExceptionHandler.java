package cn.hwjcode.straberryguild.common.exception;

import cn.hwjcode.straberryguild.common.exception.BusinessException;
import cn.hwjcode.straberryguild.common.results.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return Result.fail(ex);
    }

    /** 参数校验异常 - @Valid @RequestBody */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return Result.fail(400, message);
    }

    /** 参数校验异常 - @Validated @RequestParam */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("参数校验失败: {}", ex.getMessage());
        return Result.fail(400, ex.getMessage());
    }

    /** 参数绑定异常 */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        return Result.fail(400, message);
    }

    /** Security 认证失败 */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return Result.fail(401, "用户名或密码错误");
    }

    /** 接口不存在 404 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("接口不存在: {}", ex.getResourcePath());
        return Result.fail(404, "接口不存在: " + ex.getResourcePath());
    }

    /** 其他所有异常 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.fail(500, "系统内部错误: " + ex.getMessage());
    }
}