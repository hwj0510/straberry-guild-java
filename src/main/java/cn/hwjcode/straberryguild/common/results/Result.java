package cn.hwjcode.straberryguild.common.results;

import cn.hwjcode.straberryguild.common.exception.BusinessException;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应封装
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：200 成功，其他失败 */
    private Integer code;
    /** 提示信息 */
    private String message;
    /** 响应数据 */
    private T data;
    /** 时间戳 */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /** 成功 */
    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }

    /** 成功携带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功携带数据和自定义消息 */
    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(200, message, data);
    }

    /** 失败 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 失败（自定义状态码） */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败（业务异常） */
    public static <T> Result<T> fail(BusinessException ex) {
        return new Result<>(ex.getCode(), ex.getMessage(), null);
    }
}
