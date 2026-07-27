package hanium.damso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResultDTO<T>(Status status, String code, T data) {
    public enum Status {
        success,
        error
    }

    public static <T> ResultDTO<T> success(String code, T data) {
        return new ResultDTO<>(Status.success, code, data);
    }

    public static <T> ResultDTO<T> success(String code) {
        return success(code, null);
    }

    public static <T> ResultDTO<T> error(String code, T data) {
        return new ResultDTO<>(Status.error, code, data);
    }

    public static <T> ResultDTO<T> error(String code) {
        return error(code, null);
    }
}