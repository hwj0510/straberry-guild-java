package cn.hwjcode.straberryguild.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    // ========== 常用业务异常快捷方法 ==========

    /** 未授权 401 */
    public static BusinessException unauthorized(String msg) {
        return new BusinessException(401, msg);
    }

    /** 禁止访问 403 */
    public static BusinessException forbidden(String msg) {
        return new BusinessException(403, msg);
    }

    /** 参数错误 400 */
    public static BusinessException badRequest(String msg) {
        return new BusinessException(400, msg);
    }

    /** 资源不存在 404 */
    public static BusinessException notFound(String msg) {
        return new BusinessException(404, msg);
    }
}